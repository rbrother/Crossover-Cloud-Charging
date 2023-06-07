"use strict";
Object.defineProperty(exports, "__esModule", { value: true })
const redis = require("redis")
const util = require("util")
const KEY = `account1/balance`
const DEFAULT_BALANCE = 100

exports.chargeRequestRedis = async function (input) {
    const redisClient = await getRedisClient()
    var charges = getCharges()
    var remainingBalance = await chargeRedis(redisClient, KEY, charges)
    if (remainingBalance >= 0) {
        await disconnectRedis(redisClient)
        return {
            remainingBalance,
            charges,
            isAuthorized: true
        }
    }
    // Account went negative: undo decrement and reject request
    remainingBalance = await undoChargeRedis(redisClient, KEY, charges)
    return {
        // No need to show transient negative values
        remainingBalance : remainingBalance < 0 ? 0 : remainingBalance,
        charges : 0,
        isAuthorized : false
    }
}

async function getRedisClient() {
    return new Promise((resolve, reject) => {
        try {
            const client = new redis.RedisClient({
                host: process.env.ENDPOINT,
                port: parseInt(process.env.PORT || "6379"),
            })
            client.on("ready", () => {
                console.log('redis client ready')
                resolve(client)
            })
        }
        catch (error) {
            reject(error)
        }
    })
}

async function disconnectRedis(client) {
    return new Promise((resolve, reject) => {
        client.quit((error, res) => {
            if (error) {
                reject(error)
            }
            else if (res == "OK") {
                console.log('redis client disconnected')
                resolve(res)
            }
            else {
                reject("unknown error closing redis connection.")
            }
        })
    })
}

function getCharges() {
    return DEFAULT_BALANCE / 20
}

async function chargeRedis(redisClient, key, charges) {
    return util.promisify(redisClient.decrby).bind(redisClient).call(redisClient, key, charges)
}

async function undoChargeRedis(redisClient, key, charges) {
    return util.promisify(redisClient.incrby).bind(redisClient).call(redisClient, key, charges)
}
