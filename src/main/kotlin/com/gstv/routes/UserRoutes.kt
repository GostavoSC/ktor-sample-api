package com.gstv.routes

import com.gstv.models.User
import com.gstv.models.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun Route.userRouting() {
    route("/user") {
        get {
            val users = transaction {
                Users.selectAll().map { Users.toUser(it) }
            }

            return@get call.respond(users)
        }

        get("{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                message = "User not found",
                status = HttpStatusCode.NotFound
            )
            val user: List<User> = transaction { Users.select {  Users.id eq id }.map { Users.toUser(it) } }
            if (user.isNotEmpty()) return@get call.respond(user)

            return@get call.respond(
                message = "User not found",
                status = HttpStatusCode.NotFound
            )
        }

        delete ("{id}"){

            val id = call.parameters["id"] ?: return@delete call.respond(
                message = "Insert user ID to delete a user",
                status = HttpStatusCode.BadRequest
            )
            val delete = transaction {
                Users.deleteWhere { Users.id eq id }
            }
            if (delete == 1) return@delete call.respond(message = "Deleted", status = HttpStatusCode.OK)

            return@delete call.respond(message = "User not found",status = HttpStatusCode.NotFound)

        }

        post {
            val user = call.receive<User>()
            user.id = UUID.randomUUID().toString()

            transaction {
                Users.insert {
                    it[id] = user.id!!
                    it[name] = user.name
                }
            }
            call.respond(message = "Created",status = HttpStatusCode.Created)
        }
    }
}

fun Application.registerUserRoutes(){
    routing {
        userRouting()
    }
}