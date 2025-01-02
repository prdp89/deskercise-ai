package com.ai.app.move.deskercise.network

import okio.IOException

class ServerException(message: String) : IOException(message)

class RefreshTokenException(message: String) : IOException(message)

class NetworkException(message: String) : IOException(message)
