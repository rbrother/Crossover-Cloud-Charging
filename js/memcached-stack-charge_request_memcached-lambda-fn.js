"use strict";
Object.defineProperty(exports, "__esModule", { value: true })
const memcached = require("memcached")
const KEY = `account1/balance`
const DEFAULT_BALANCE = 100
const memcachedClient = new memcached(`${process.env.ENDPOINT}:${process.env.PORT}`)

exports.chargeRequestMemcached = async function (input) {
    return await tryChargeMemcached()
}

async function tryChargeMemcached() {
    var memcachedResponse = await getBalanceMemcached(KEY)
    var remainingBalance = memcachedResponse[KEY]
    var casToken = memcachedResponse.cas
    const charges = getCharges()
    const isAuthorized = authorizeRequest(remainingBalance, charges)
    if (!isAuthorized) {
        return {
            remainingBalance,
            isAuthorized,
            charges: 0,
        }
    }
    const newBalance = remainingBalance - charges
    const success = await memcachedCAS(KEY, newBalance, casToken)
    if (success === true) {
        return {
            remainingBalance : newBalance,
            charges,
            isAuthorized,
        }
    } else {
      // Some other client got in between: retry
      return await tryChargeMemcached()
    }
}

function authorizeRequest(remainingBalance, charges) {
    return remainingBalance >= charges
}

function getCharges() {
    return DEFAULT_BALANCE / 20
}

async function getBalanceMemcached(key) {
    return new Promise((resolve, reject) => {
        memcachedClient.gets(key, (err, data) => {
            if (err) {
                reject(err)
            }
            else {
                resolve(data)
            }
        })
    })
}

async function memcachedCAS(key, value, token) {
    return new Promise((resolve, reject) => {
        memcachedClient.cas(key, value, token, 10000, (err, success) => {
            if (err) {
                reject(err)
            }
            else {
                resolve(success)
            }
        })
    })
}
