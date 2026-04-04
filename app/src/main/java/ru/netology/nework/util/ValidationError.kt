package ru.netology.nework.util

sealed class ValidationError {
    object EmptyLogin : ValidationError()
    object LoginTooShort : ValidationError()
    object EmptyName : ValidationError()
    object EmptyPassword : ValidationError()
    object PasswordTooShort : ValidationError()
    object PasswordsDontMatch : ValidationError()
    object AvatarInvalid : ValidationError()
}