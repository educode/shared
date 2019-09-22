package de.hhu.educode.shared.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void testInvalidValueIsGuest() {
        var role = Role.from("not_a_role");
        assertThat(role).isEqualTo(Role.GUEST);
    }

    @Test
    void testCovers() {
        assertThat(Role.ADMIN.covers(Role.GUEST)).isTrue();
        assertThat(Role.GUEST.covers(Role.ADMIN)).isFalse();
    }
}