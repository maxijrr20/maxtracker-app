package com.maxtracker.api.security;

import java.time.Instant;

/** Token firmado junto con su instante de expiración. */
public record TokenJwt(String valor, Instant expiraEn) {
}
