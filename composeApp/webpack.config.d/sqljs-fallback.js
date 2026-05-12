// composeApp/webpack.config.d/webpack.config.js
module.exports = function (config) {
    config.resolve = config.resolve || {};
    config.resolve.fallback = {
        ...config.resolve.fallback,
        fs: false,
        path: false,
        crypto: false,
    };
    return config;
};