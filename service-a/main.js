const fs = require('fs');
const jwt = require('jsonwebtoken');
const express = require('express');
const axios = require('axios');

const app = express();
app.use(express.json());

const PRIVATE_KEY = fs.readFileSync('./serviceA_private.pem');
const SERVICE_B_PUBLIC_KEY = fs.readFileSync('./serviceB_public.pem');

// Generate JWT signed with A's private key
function createJWT(payload) {
  return jwt.sign(payload, PRIVATE_KEY, {
    algorithm: 'RS256',
    expiresIn: '30m',
    issuer: 'service-a',
    audience: 'service-b',
  });
}

// Verify JWT from Service B
function verifyToken(token) {
  try {
    const payload = jwt.verify(token, SERVICE_B_PUBLIC_KEY, {
      algorithms: ['RS256'],
      issuer: 'service-b',
      audience: 'service-a',
    });
    return payload;
  } catch (err) {
    return null;
  }
}

// Protected endpoint for Service B
app.post('/api/data', (req, res) => {
  const token = req.headers.authorization?.split(' ')[1];
  const payload = verifyToken(token);
  if (!payload) return res.status(401).json({ error: 'Invalid token' });

  res.json({ message: 'Hello from Service A', by: payload.sub });
});

// Client that calls Service B
app.get('/call-b', async (req, res) => {
  const token = createJWT({ sub: 'service-a-user', role: 'internal' });
  try {
    const response = await axios.get('http://localhost:3001/api/protected', {
      headers: { Authorization: `Bearer ${token}` },
    });
    console.log(token);
    console.log(response.data);
    res.json(response.data);
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

app.listen(3000, () => {
  console.log('Service A running at http://localhost:3000');
});
