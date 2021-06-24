const express = require('express')
const app = express()
const port = 3000

app.get('/', (req, res) => {
    res.send('Hello Circle!')
})

app.listen(port, () => {
    console.log(`The Circle web UI started on port ${port}`)
})
