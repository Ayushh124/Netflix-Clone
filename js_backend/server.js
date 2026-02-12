require('dotenv').config();

const express = require('express');
const { Sequelize, DataTypes, Op } = require('sequelize');
const bcrypt = require('bcryptjs');
const session = require('express-session');
const SequelizeStore = require('connect-session-sequelize')(session.Store);
const bodyParser = require('body-parser');
const cors = require('cors');
const { OAuth2Client } = require('google-auth-library');
const passport = require('passport');
const GitHubStrategy = require('passport-github2').Strategy;

const app = express();
app.use(cors({
    origin: true, // Allow all origins for now (or specific Android IP)
    credentials: true // Important for cookies
}));
app.use(bodyParser.json());

/* ✅ Secrets */
const SECRET_KEY = process.env.JWT_SECRET || 'your_secret_key';
const GOOGLE_CLIENT_ID = process.env.GOOGLE_CLIENT_ID || '';
const GITHUB_CLIENT_ID = process.env.GITHUB_CLIENT_ID || '';
const GITHUB_CLIENT_SECRET = process.env.GITHUB_CLIENT_SECRET || '';

/* ✅ Initialize Google OAuth Client */
const client = new OAuth2Client(GOOGLE_CLIENT_ID);

/* ---------------------------------------------------------------------- */
/* 1. DATABASE CONNECTION */
/* ---------------------------------------------------------------------- */
const sequelize = new Sequelize('netflix_clone', 'root', '123456', {
    host: 'localhost',
    dialect: 'mysql'
});

/* ✅ Session Store */
const sessionStore = new SequelizeStore({
    db: sequelize,
});

app.use(session({
    secret: SECRET_KEY,
    store: sessionStore,
    resave: false,
    saveUninitialized: false,
    cookie: {
        secure: false, // Set to true if using HTTPS
        httpOnly: true,
        maxAge: 1000 * 60 * 60 * 24 * 7 // 1 week
    }
}));

sessionStore.sync();

/* ---------------------------------------------------------------------- */
/* 1.5. PASSPORT CONFIGURATION */
/* ---------------------------------------------------------------------- */

/* ✅ Initialize Passport */
app.use(passport.initialize());
app.use(passport.session());

/* ✅ Passport Serialization (Session-based) */
passport.serializeUser((user, done) => {
    done(null, user.id);
});

passport.deserializeUser(async (id, done) => {
    try {
        const user = await User.findByPk(id);
        done(null, user);
    } catch (err) {
        done(err, null);
    }
});

/* ✅ GitHub Strategy */
passport.use(new GitHubStrategy({
    clientID: GITHUB_CLIENT_ID,
    clientSecret: GITHUB_CLIENT_SECRET,
    callbackURL: "http://localhost:3002/auth/github/callback"
},
async (accessToken, refreshToken, profile, done) => {
    try {
        const githubId = profile.id;
        const email = profile.emails && profile.emails[0] ? profile.emails[0].value : `${githubId}@github.user`;

        console.log("✅ GitHub User:", email, "ID:", githubId);

        let user = await User.findOne({ where: { email } });

        if (!user) {
            user = await User.create({
                email: email,
                githubId: githubId,
                subscribe: true
            });
            console.log("🆕 New GitHub user created");
        } else {
            if (!user.githubId) {
                user.githubId = githubId;
                await user.save();
            }
        }

        return done(null, user);
    } catch (err) {
        console.error("❌ GitHub Auth Error:", err.message);
        return done(err, null);
    }
}));

/* ---------------------------------------------------------------------- */
/* 2. MODELS */
/* ---------------------------------------------------------------------- */
const User = sequelize.define('User', {
    id: { type: DataTypes.INTEGER, autoIncrement: true, primaryKey: true },
    name: { type: DataTypes.STRING },
    email: { type: DataTypes.STRING, allowNull: false, unique: true },
    password: { type: DataTypes.STRING },
    googleId: { type: DataTypes.STRING },
    githubId: { type: DataTypes.STRING },
    subscribe: { type: DataTypes.BOOLEAN, defaultValue: false }
});

const Movie = sequelize.define('Movie', {
    id: { type: DataTypes.INTEGER, autoIncrement: true, primaryKey: true },
    title: { type: DataTypes.STRING, allowNull: false },
    description: { type: DataTypes.TEXT },
    video_url: { type: DataTypes.STRING, allowNull: false },
    thumbnail_url: { type: DataTypes.STRING },
    category: { type: DataTypes.STRING }
}, {
    tableName: 'movies',
    timestamps: true,
    createdAt: 'created_at',
    updatedAt: false
});

const Tag = sequelize.define('Tag', {
    id: { type: DataTypes.INTEGER, autoIncrement: true, primaryKey: true },
    name: { type: DataTypes.STRING, allowNull: false, unique: true }
}, {
    tableName: 'tags',
    timestamps: false
});

const MovieTag = sequelize.define('MovieTag', {
    movieId: {
        type: DataTypes.INTEGER,
        references: {
            model: Movie,
            key: 'id'
        }
    },
    tagId: {
        type: DataTypes.INTEGER,
        references: {
            model: Tag,
            key: 'id'
        }
    }
}, {
    tableName: 'movie_tags',
    timestamps: false
});

Movie.belongsToMany(Tag, { through: MovieTag, foreignKey: 'movie_id', otherKey: 'tag_id' });
Tag.belongsToMany(Movie, { through: MovieTag, foreignKey: 'tag_id', otherKey: 'movie_id' });

sequelize.sync({ alter: true })
    .then(() => console.log('✅ MySQL Database Synced'))
    .catch(err => console.error('❌ MySQL Sync Error:', err));

/* ---------------------------------------------------------------------- */
/* 3. MIDDLEWARE */
/* ---------------------------------------------------------------------- */
const isAuthenticated = (req, res, next) => {
    if (req.session.userId) {
        next();
    } else {
        res.status(401).json({ message: 'Unauthorized' });
    }
};

/* ---------------------------------------------------------------------- */
/* 4. AUTH ROUTES */
/* ---------------------------------------------------------------------- */

/* ✅ Register */
app.post('/auth/register', async (req, res) => {
    try {
        const { email, password, name } = req.body;

        if (!email || !password)
            return res.status(400).json({ message: 'Email and password required' });

        const existingUser = await User.findOne({ where: { email } });

        if (existingUser)
            return res.status(400).json({ message: 'User already exists' });

        const hashedPassword = await bcrypt.hash(password, 10);

        const newUser = await User.create({
            name: name || null,
            email,
            password: hashedPassword,
            subscribe: true
        });

        console.log(`✅ New user registered: ${email} (Name: ${name || 'Not provided'})`);

        req.session.userId = newUser.id;

        res.json({
            message: 'User registered',
            subscribed: newUser.subscribe,
            token: "session_active" // Dummy token for client compatibility
        });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

/* ✅ Normal Login */
app.post('/auth/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        const user = await User.findOne({ where: { email } });

        if (!user)
            return res.status(401).json({ message: 'Invalid credentials' });

        const match = await bcrypt.compare(password, user.password);

        if (!match)
            return res.status(401).json({ message: 'Invalid credentials' });

        req.session.userId = user.id;

        res.json({
            message: 'Login successful',
            subscribed: user.subscribe,
            token: "session_active" // Dummy token for client compatibility
        });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});



/* ✅ GOOGLE LOGIN (IMPORTANT) */
app.post('/auth/google', async (req, res) => {
    try {
        const { idToken } = req.body;

        if (!idToken)
            return res.status(400).json({ message: 'ID Token missing' });

        /* ✅ Verify with Google */
        const ticket = await client.verifyIdToken({
            idToken: idToken,
            audience: GOOGLE_CLIENT_ID,
        });

        const payload = ticket.getPayload();

        const googleId = payload.sub;
        const email = payload.email;

        console.log("✅ Google User:", email);

        let user = await User.findOne({ where: { email } });

        if (!user) {
            user = await User.create({
                email: email,
                googleId: googleId,
                subscribe: true
            });

            console.log("🆕 New Google user created");
        } else {
            if (!user.googleId) {
                user.googleId = googleId;
                await user.save();
            }
        }

        req.session.userId = user.id;

        res.json({
            message: 'Google login successful',
            subscribed: user.subscribe,
            token: "session_active" // Dummy token for client compatibility
        });

    } catch (err) {
        console.error("❌ Google Auth Error:", err.message);
        res.status(401).json({ message: 'Invalid Google Token' });
    }
});

/* ✅ GitHub OAuth Routes */
app.get('/auth/github', 
    passport.authenticate('github', { scope: ['user:email'] })
);

app.get('/auth/github/callback',
    passport.authenticate('github', { failureRedirect: '/login' }),
    (req, res) => {
        // Successful authentication, set session
        req.session.userId = req.user.id;
        
        console.log("✅ GitHub login successful, userId:", req.user.id);
        console.log("📱 Session ID:", req.sessionID);
        
        // Redirect to Android app deep link
        // The app will verify the session by making an authenticated request
        res.redirect(`netflixclone://auth?success=true&session=${req.sessionID}`);
    }
);

/* ---------------------------------------------------------------------- */
/* 5. CONTENT ROUTES */
/* ---------------------------------------------------------------------- */

/* ✅ Get All Tags */
app.get('/tags', isAuthenticated, async (req, res) => {
    try {
        const tags = await Tag.findAll();
        res.json(tags);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

/* ✅ Get Movies (Filter by Multiple Tags and/or Search) */
app.get('/movies', isAuthenticated, async (req, res) => {
    try {
        const { tag, tags, search } = req.query;
        let options = {
            include: [{
                model: Tag,
                through: { attributes: [] } // Hide junction table
            }],
            where: {}
        };

        // Handle multi-select tag filtering (tags=1,2,3)
        if (tags) {
            const tagIds = tags.split(',').map(id => parseInt(id.trim())).filter(id => !isNaN(id));
            
            if (tagIds.length > 0) {
                console.log(`🏷️  Filtering by tag IDs: ${tagIds.join(', ')}`);
                options.include[0].where = {
                    id: { [Op.in]: tagIds }
                };
                options.include[0].required = true; // INNER JOIN to only get movies with these tags
            }
        }
        // Legacy single tag support (tag=Action)
        else if (tag) {
            console.log(`🏷️  Filtering by tag name: ${tag}`);
            options.include[0].where = { name: tag };
            options.include[0].required = true;
        }

        // Case-insensitive search on title and description
        if (search) {
            console.log(`🔍 Searching for: ${search}`);
            const searchLower = search.toLowerCase();
            options.where = {
                [Op.or]: [
                    sequelize.where(
                        sequelize.fn('LOWER', sequelize.col('title')),
                        'LIKE',
                        `%${searchLower}%`
                    ),
                    sequelize.where(
                        sequelize.fn('LOWER', sequelize.col('description')),
                        'LIKE',
                        `%${searchLower}%`
                    )
                ]
            };
        }

        const movies = await Movie.findAll(options);
        
        console.log(`✅ Found ${movies.length} movies`);
        if (movies.length > 0) {
            console.log('📋 Movie titles:', movies.map(m => m.title).join(', '));
        }
        res.json(movies);
    } catch (err) {
        console.error('❌ Error fetching movies:', err.message);
        res.status(500).json({ error: err.message });
    }
});

/* ✅ Get Featured Movies (4 working videos) */
app.get('/movies/featured', isAuthenticated, async (req, res) => {
    try {
        console.log('🌟 Fetching featured movies (IDs 1-4)');
        const featuredMovies = await Movie.findAll({
            where: {
                id: { [Op.in]: [1, 2, 3, 4] }
            },
            include: [{
                model: Tag,
                through: { attributes: [] }
            }],
            order: [['id', 'ASC']]
        });
        
        console.log(`✅ Found ${featuredMovies.length} featured movies`);
        res.json(featuredMovies);
    } catch (err) {
        console.error('❌ Error fetching featured movies:', err.message);
        res.status(500).json({ error: err.message });
    }
});

/* ✅ Get Movie Details */
app.get('/movies/:id', isAuthenticated, async (req, res) => {
    try {
        const { id } = req.params;
        const movie = await Movie.findByPk(id, {
            include: [{
                model: Tag,
                through: { attributes: [] }
            }]
        });

        if (!movie) {
            return res.status(404).json({ message: 'Movie not found' });
        }

        res.json(movie);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

/* ✅ SECURE VIDEO STREAMING ENDPOINT */
/* This endpoint ensures only authenticated users can access video streams */
app.get('/stream/:movieId', isAuthenticated, async (req, res) => {
    try {
        const { movieId } = req.params;
        
        console.log(`🎬 Stream request for movie ID: ${movieId} by user: ${req.session.userId}`);
        
        // Fetch movie from database
        const movie = await Movie.findByPk(movieId);
        
        if (!movie) {
            console.log(`❌ Movie not found: ${movieId}`);
            return res.status(404).json({ message: 'Movie not found' });
        }
        
        if (!movie.video_url) {
            console.log(`❌ No video URL for movie: ${movieId}`);
            return res.status(404).json({ message: 'Video not available' });
        }
        
        console.log(`✅ Returning video URL for authenticated user: ${movie.video_url}`);
        
        // Return the video URL as JSON
        // The client will use this URL with ExoPlayer
        res.json({ 
            videoUrl: movie.video_url,
            movieId: movie.id,
            title: movie.title
        });
        
    } catch (err) {
        console.error('❌ Stream error:', err.message);
        res.status(500).json({ error: 'Failed to stream video' });
    }
});

/* ✅ DEBUG: Get All Movie Titles (for testing) */
app.get('/debug/movies', async (req, res) => {
    try {
        const movies = await Movie.findAll({
            attributes: ['id', 'title', 'description'],
            order: [['id', 'ASC']]
        });
        res.json(movies);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

/* ✅ Add Movie with Tags (Admin/Test) */
app.post('/movies', async (req, res) => {
    try {
        const { title, description, video_url, thumbnail_url, category, tags } = req.body;

        const movie = await Movie.create({
            title, description, video_url, thumbnail_url, category
        });

        if (tags && tags.length > 0) {
            for (const tagName of tags) {
                const [tag] = await Tag.findOrCreate({ where: { name: tagName } });
                await movie.addTag(tag);
            }
        }

        res.json({ message: 'Movie added', movie });
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});
/* ---------------------------------------------------------------------- */
/* SERVER */
/* ---------------------------------------------------------------------- */
const PORT = process.env.PORT || 3000;

app.listen(PORT, '0.0.0.0', () => {
    console.log(`🚀 Server running on port ${PORT}`);

    try {
        const { networkInterfaces } = require('os');
        const nets = networkInterfaces();

        for (const name of Object.keys(nets)) {
            for (const net of nets[name]) {
                if (net.family === 'IPv4' && !net.internal) {
                    console.log(`📡 Local IP: http://${net.address}:${PORT}/`);
                }
            }
        }
    } catch (err) {
        console.log(`📡 Server is running on http://localhost:${PORT}/`);
    }

    console.log(`💻 Emulator: http://10.0.2.2:${PORT}/`);
});
