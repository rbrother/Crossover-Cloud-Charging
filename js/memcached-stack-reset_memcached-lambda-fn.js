"use strict";
Object.defineProperty(exports, "__esModule", { value: true })
const memcached = require("memcached")
const KEY = `account1/balance`
const DEFAULT_BALANCE = 100
const MAX_EXPIRATION = 60 * 60 * 24 * 30
const memcachedClient = new memcached(`${process.env.ENDPOINT}:${process.env.PORT}`)

exports.resetMemcached = async function () {
    var ret = new Promise((resolve, reject) => {
        memcachedClient.set(KEY, DEFAULT_BALANCE, MAX_EXPIRATION, (error, res) => {
            if (error) {
                reject(error)
            }
            else {
                resolve(DEFAULT_BALANCE)
            }
        })
    })
    return ret
}
