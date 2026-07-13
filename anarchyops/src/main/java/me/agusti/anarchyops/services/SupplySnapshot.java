package me.agusti.anarchyops.services;

public record SupplySnapshot(
    String name,
    long createdAt,
    int totems,
    int rockets,
    int pearls,
    int obsidian,
    int crystals,
    int goldenApples,
    int experienceBottles
) {}
