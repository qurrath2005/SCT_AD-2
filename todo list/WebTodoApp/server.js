const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;

const MIME_TYPES = {
    '.html': 'text/html',
    '.css': 'text/css',
    '.js': 'text/javascript',
    '.json': 'application/json',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.gif': 'image/gif',
    '.svg': 'image/svg+xml',
    '.ico': 'image/x-icon',
};

const server = http.createServer((req, res) => {
    console.log(`${req.method} ${req.url}`);
    
    // Handle favicon.ico request
    if (req.url === '/favicon.ico') {
        res.statusCode = 204; // No content
        res.end();
        return;
    }
    
    // Normalize URL to prevent directory traversal attacks
    let filePath = path.normalize(
        path.join(__dirname, req.url === '/' ? 'index.html' : req.url)
    );
    
    // Check if the path is within the current directory
    if (!filePath.startsWith(__dirname)) {
        res.statusCode = 403; // Forbidden
        res.end('Forbidden');
        return;
    }
    
    const extname = path.extname(filePath);
    const contentType = MIME_TYPES[extname] || 'application/octet-stream';
    
    fs.readFile(filePath, (err, content) => {
        if (err) {
            if (err.code === 'ENOENT') {
                // File not found
                res.statusCode = 404;
                res.end('File not found');
            } else {
                // Server error
                res.statusCode = 500;
                res.end(`Server Error: ${err.code}`);
            }
        } else {
            // Success
            res.statusCode = 200;
            res.setHeader('Content-Type', contentType);
            res.end(content);
        }
    });
});

server.listen(PORT, () => {
    console.log(`Server running at http://localhost:${PORT}/`);
    console.log(`Press Ctrl+C to stop the server`);
});
