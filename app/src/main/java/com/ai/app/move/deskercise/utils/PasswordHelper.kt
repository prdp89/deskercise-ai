package com.ai.app.move.deskercise.utils

/**
 * File that provides helper functions for password validation
 */

/**
 * Return true if all of the checks in [CharSequence.passwordCheck] pass
 */
fun CharSequence.isValidPassword(): Boolean {
    val pV = this.passwordCheck()
    return (
            pV.hasLength &&
            pV.hasDigit &&
            pV.hasSpecialChar &&
            pV.hasLowercase &&
            pV.hasUppercase
            )
}

/**
 * Checks whether the password is strong enough
 *
 * @return [PasswordValidator]
 */
fun CharSequence.passwordCheck(): PasswordValidator {
    val pV = PasswordValidator()

    pV.hasLength = this.length >= 8
    pV.hasDigit = this.any { it.isDigit() }
    val specialChars = setOf(
        '!',
        '@',
        '#',
        '$',
        '%',
        '^',
        '&',
        '*',
        '(',
        ')',
        '_',
        '+',
        '-',
        '=',
        '{',
        '}',
        '|',
        '[',
        ']',
        '\\',
        ':',
        ';',
        '\"',
        '\'',
        '<',
        '>',
        ',',
        '.',
        '?',
        '/',
    )
    pV.hasSpecialChar = this.any { it in specialChars }
    pV.hasUppercase = this.any { it.isUpperCase() }
    pV.hasLowercase = this.any { it.isLowerCase() }

    return pV
}

/**
 * Provides a list of boolean values to indicate whether a specific
 * password check passes or not
 */
data class PasswordValidator (
    // Password has above a certain amount of characters
    var hasLength: Boolean = false,

    // Password contains a certain number of digits
    var hasDigit: Boolean = false,

    // Password contains a certain number of special characters
    var hasSpecialChar: Boolean = false,

    // Password contains a certain number of uppercase letters
    var hasUppercase: Boolean = false,

    // Password contains a certain number of lowercase letters
    var hasLowercase: Boolean = false
)