const path = require('path');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const sqliteWasmDir = path.resolve(__dirname, '../../node_modules/@sqlite.org/sqlite-wasm/sqlite-wasm/jswasm/');

config.plugins.push(
    new CopyWebpackPlugin({
        patterns: [
            { from: path.join(sqliteWasmDir, 'sqlite3.wasm'), to: 'sqlite3.wasm' },
            { from: path.join(sqliteWasmDir, 'sqlite3.js'), to: 'sqlite3.js' },
            { from: path.join(sqliteWasmDir, 'sqlite3-opfs-async-proxy.js'), to: 'sqlite3-opfs-async-proxy.js' }
        ]
    })
);

config.devServer = config.devServer || {};
config.devServer.headers = config.devServer.headers || {};
config.devServer.headers["Cross-Origin-Opener-Policy"] = "same-origin";
config.devServer.headers["Cross-Origin-Embedder-Policy"] = "require-corp";