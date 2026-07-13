package com.brunobeduschi.duelarena.duel;

import java.util.UUID;

public record DuelRequest(UUID challenger, UUID target) {
}
