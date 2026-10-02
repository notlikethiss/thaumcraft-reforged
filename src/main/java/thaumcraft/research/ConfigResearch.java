package thaumcraft.research;

import thaumcraft.aspect.Aspect;
import thaumcraft.aspect.AspectList;

public final class ConfigResearch {
    private ConfigResearch() {
    }

    private static AspectList tags() {
        return new AspectList();
    }

    public static void registerAll() {
        ResearchItem researchAlumentum;
        ResearchItem researchArcaneBellows;
        ResearchItem researchArcaneBore;
        ResearchItem researchArcaneDoor;
        ResearchItem researchArcaneEar;
        ResearchItem researchArcanePlate;
        ResearchItem researchBasicAlchemy;
        ResearchItem researchBasicArtiface;
        ResearchItem researchBasicFlux;
        ResearchItem researchBasicTransmutation;
        ResearchItem researchBootsTraveller;
        ResearchItem researchCrystalCapacitor;
        ResearchItem researchCrystalCluster;
        ResearchItem researchCrystalCore;
        ResearchItem researchElementalAxe;
        ResearchItem researchElementalHoe;
        ResearchItem researchElementalPickAxe;
        ResearchItem researchElementalShovel;
        ResearchItem researchElementalSword;
        ResearchItem researchFabric;
        ResearchItem researchGoggles;
        ResearchItem researchGolemClay;
        ResearchItem researchGolemClayAdv;
        ResearchItem researchGolemFast;
        ResearchItem researchGolemIronGuardian;
        ResearchItem researchGolemSight;
        ResearchItem researchGolemSmart;
        ResearchItem researchGolemStone;
        ResearchItem researchGolemStoneAdv;
        ResearchItem researchGolemStraw;
        ResearchItem researchGolemStrong;
        ResearchItem researchGolemTallow;
        ResearchItem researchGolemTallowAdv;
        ResearchItem researchGolemancy;
        ResearchItem researchGunpowder;
        ResearchItem researchHandMirror;
        ResearchItem researchHellrod;
        ResearchItem researchHoverHarness;
        ResearchItem researchHungryChest;
        ResearchItem researchInfernalFurnace;
        ResearchItem researchJar;
        ResearchItem researchJarBrain;
        ResearchItem researchLevitator;
        ResearchItem researchMagicBlock;
        ResearchItem researchMirror;
        ResearchItem researchNitor;
        ResearchItem researchPortableHole;
        ResearchItem researchRobes;
        ResearchItem researchTTOE;
        ResearchItem researchTallow;
        ResearchItem researchThaumium;
        ResearchItem researchThaumometer;
        ResearchItem researchTinyArmor;
        ResearchItem researchTinyBowTie;
        ResearchItem researchTinyDart;
        ResearchItem researchTinyFez;
        ResearchItem researchTinyGlasses;
        ResearchItem researchTinyHat;
        ResearchItem researchTinyVisor;
        ResearchItem researchTransmutationCopper;
        ResearchItem researchTransmutationIron;
        ResearchItem researchTransmutationLead;
        ResearchItem researchTransmutationSilver;
        ResearchItem researchTransmutationTin;
        ResearchItem researchTravelStone;
        ResearchItem researchUTFT;
        ResearchItem researchWandExcavate;
        ResearchItem researchWandFire;
        ResearchItem researchWandFrost;
        ResearchItem researchWandLightning;
        ResearchItem researchWandTrade;

        
        new ResearchItem("ASPECT", tags(), -6, -8, 9).setAutoUnlock().registerResearchItem();
        new ResearchItem("AURA", tags(), -6, -7, 7).setAutoUnlock().registerResearchItem();
        new ResearchItem("INFUSEDORE", tags(), -6, -5, "thaumcraft:vis_infused_stone").setAutoUnlock().registerResearchItem();
        new ResearchItem("FLUX", tags(), -6, -6, 8).setAutoUnlock().registerResearchItem();
        new ResearchItem("GREATWOOD", tags(), -7, -8, "thaumcraft:greatwood_sapling").setAutoUnlock().registerResearchItem();
        new ResearchItem("SILVERWOOD", tags(), -7, -7, "thaumcraft:silverwood_sapling").setAutoUnlock().registerResearchItem();
        new ResearchItem("SHIMMERLEAF", tags(), -7, -6, "thaumcraft:shimmerleaf").setAutoUnlock().registerResearchItem();
        new ResearchItem("CINDERPEARL", tags(), -7, -5, "thaumcraft:cinderpearl").setAutoUnlock().registerResearchItem();
        new ResearchItem("WAND", tags(), -3, -5, "thaumcraft:wand_apprentice").setAutoUnlock().registerResearchItem();
        new ResearchItem("ACTABLE", tags(), -3, -7, "thaumcraft:arcane_worktable").setAutoUnlock().registerResearchItem();
        new ResearchItem("TABLE", tags(), -3, -6, "thaumcraft:table").setAutoUnlock().registerResearchItem();
        new ResearchItem("RESTABLE", tags(), -3, -8, 6).setAutoUnlock().registerResearchItem();
        new ResearchItem("KNOWLEDGE", tags(), -3, -9, "thaumcraft:knowledge_fragment").setAutoUnlock().registerResearchItem();
        new ResearchItem("CRUCIBLE", tags(), -2, -6, "thaumcraft:crucible").setAutoUnlock().registerResearchItem();
        new ResearchItem("PHIAL", tags(), -2, -7, "thaumcraft:essentia_phial").setAutoUnlock().registerResearchItem();
        new ResearchItem("THAUMONOMICON", tags(), -4, -6, "thaumcraft:thaumonomicon").setAutoUnlock().registerResearchItem();
        new ResearchItem("SCRIBE", tags(), -4, -7, "thaumcraft:scribing_tools").setAutoUnlock().registerResearchItem();
        new ResearchItem("ENCHANTS", tags(), -4, -8, "minecraft:enchanted_book").setAutoUnlock().registerResearchItem();
        researchBasicAlchemy = new ResearchItem("BALC", tags(), 1, 0, 5).setStub();
        researchBasicTransmutation = new ResearchItem("BASTRANS", tags().add(Aspect.METAL, 16).add(Aspect.VALUABLE, 8).add(Aspect.EXCHANGE, 5), 3, -2, "minecraft:gold_nugget")
            .setSiblings(researchBasicAlchemy)
            .registerResearchItem();
        researchTransmutationIron = new ResearchItem("TRANSIRON", tags().add(Aspect.METAL, 20).add(Aspect.EXCHANGE, 9), 3, -6, "minecraft:iron_nugget")
            .setParents(researchBasicTransmutation)
            .setHidden()
            .registerResearchItem();
        {
            researchTransmutationCopper = new ResearchItem("TRANSCOPPER", tags().add(Aspect.METAL, 16).add(Aspect.LIFE, 8).add(Aspect.EXCHANGE, 5), 2, -5, "minecraft:copper_nugget")
                .setParents(researchBasicTransmutation)
                .setHidden()
                .registerResearchItem();
        }

        {
            researchTransmutationTin = new ResearchItem("TRANSTIN", tags().add(Aspect.METAL, 16).add(Aspect.CRYSTAL, 8).add(Aspect.EXCHANGE, 5), 4, -5, "thaumcraft:tin_nugget")
                .setParents(researchBasicTransmutation)
                .setHidden()
                .registerResearchItem();
        }

        {
            researchTransmutationSilver = new ResearchItem("TRANSSILVER", tags().add(Aspect.METAL, 16).add(Aspect.EXCHANGE, 13), 1, -4, "thaumcraft:silver_nugget")
                .setParents(researchBasicTransmutation)
                .setHidden()
                .registerResearchItem();
        }

        {
            researchTransmutationLead = new ResearchItem("TRANSLEAD", tags().add(Aspect.METAL, 16).add(Aspect.VOID, 5).add(Aspect.EXCHANGE, 5), 5, -4, "thaumcraft:lead_nugget")
                .setParents(researchBasicTransmutation)
                .setHidden()
                .registerResearchItem();
        }

        researchAlumentum = new ResearchItem("ALUMENTUM", tags().add(Aspect.POWER, 8).add(Aspect.FIRE, 8).add(Aspect.DESTRUCTION, 4), 4, 0, "thaumcraft:alumentum")
            .setSiblings(researchBasicAlchemy)
            .registerResearchItem();
        researchGunpowder = new ResearchItem("GUNPOWDER", tags().add(Aspect.EXCHANGE, 8).add(Aspect.FIRE, 16).add(Aspect.DESTRUCTION, 16), 6, 0, "minecraft:gunpowder")
            .setParents(researchAlumentum)
            .setHidden()
            .registerResearchItem();
        researchNitor = new ResearchItem("NITOR", tags().add(Aspect.POWER, 8).add(Aspect.LIGHT, 8).add(Aspect.FIRE, 4), 3, 2, "thaumcraft:nitor")
            .setSiblings(researchBasicAlchemy)
            .registerResearchItem();
        researchBasicAlchemy.setParents(researchNitor, researchAlumentum, researchBasicTransmutation);
        researchBasicAlchemy.registerResearchItem();
        researchTallow = new ResearchItem("TALLOW", tags().add(Aspect.FLESH, 5).add(Aspect.FIRE, 2), 7, 3, "thaumcraft:magic_tallow")
            .setHidden()
            .registerResearchItem();
        researchThaumium = new ResearchItem("THAUMIUM", tags().add(Aspect.METAL, 16).add(Aspect.MAGIC, 8).add(Aspect.EXCHANGE, 4), 0, -2, "thaumcraft:thaumium_ingot")
            .setParents(researchBasicTransmutation)
            .registerResearchItem();
        researchBasicArtiface = new ResearchItem("BART", tags(), -1, 0, 4).setStub();
        researchMagicBlock = new ResearchItem("MAGBLOCK", tags().add(Aspect.METAL, 4).add(Aspect.MAGIC, 4).add(Aspect.CRAFT, 4).add(Aspect.ROCK, 8).add(Aspect.WOOD, 8), -3, -2, "thaumcraft:arcane_stone")
            .setParents(researchThaumium)
            .setSiblings(researchBasicArtiface)
            .registerResearchItem();
        researchThaumometer = new ResearchItem("THAUMOMETER", tags().add(Aspect.MAGIC, 8).add(Aspect.KNOWLEDGE, 8).add(Aspect.MECHANISM, 4), -3, 2, "thaumcraft:thaumometer")
            .setSiblings(researchBasicArtiface)
            .registerResearchItem();
        researchFabric = new ResearchItem("ENCHFABRIC", tags().add(Aspect.CLOTH, 8).add(Aspect.CRAFT, 4).add(Aspect.MAGIC, 4), -4, 0, "thaumcraft:enchanted_fabric")
            .setSiblings(researchBasicArtiface)
            .registerResearchItem();
        researchRobes = new ResearchItem("ROBES", tags().add(Aspect.CLOTH, 16).add(Aspect.ARMOR, 8).add(Aspect.MAGIC, 8), -6, 0, "thaumcraft:robe_chestplate")
            .setParents(researchFabric)
            .setHidden()
            .registerResearchItem();
        researchBasicArtiface.setParents(researchMagicBlock, researchThaumometer, researchFabric);
        researchBasicArtiface.registerResearchItem();
        researchUTFT = new ResearchItem("UTFT", tags()
                    .add(Aspect.KNOWLEDGE, 8)
                    .add(Aspect.FLUX, 8)
                    .add(Aspect.MAGIC, 8)
                    .add(Aspect.FIRE, 8)
                    .add(Aspect.WIND, 8)
                    .add(Aspect.EARTH, 8)
                    .add(Aspect.WATER, 8)
                    .add(Aspect.TIME, 4), 0, 2, 0)
            .setParents(researchBasicArtiface, researchBasicAlchemy)
            .setSpecial()
            .registerResearchItem();
        researchGoggles = new ResearchItem("GOGGLES", tags().add(Aspect.VISION, 16).add(Aspect.MAGIC, 16).add(Aspect.FLUX, 8).add(Aspect.KNOWLEDGE, 12).add(Aspect.ARMOR, 4), -3, 3, "thaumcraft:goggles_of_revealing")
            .setParents(researchUTFT)
            .setHidden()
            .registerResearchItem();
        researchWandFire = new ResearchItem("WANDFIRE", tags().add(Aspect.DESTRUCTION, 8).add(Aspect.MAGIC, 8).add(Aspect.WEAPON, 8).add(Aspect.FIRE, 8), 1, 4, "thaumcraft:wand_fire")
            .setParents(researchUTFT)
            .registerResearchItem();
        researchWandFrost = new ResearchItem("WANDFROST", tags().add(Aspect.DESTRUCTION, 8).add(Aspect.MAGIC, 8).add(Aspect.WEAPON, 8).add(Aspect.COLD, 8), 1, 5, "thaumcraft:wand_frost")
            .setParents(researchUTFT)
            .registerResearchItem();
        researchWandLightning = new ResearchItem("WANDLIGHTNING", tags().add(Aspect.DESTRUCTION, 8).add(Aspect.MAGIC, 8).add(Aspect.WEAPON, 8).add(Aspect.POWER, 8), 1, 6, "thaumcraft:wand_lightning")
            .setParents(researchUTFT)
            .registerResearchItem();
        researchWandExcavate = new ResearchItem("WANDEXCAVATE", tags().add(Aspect.METAL, 8).add(Aspect.MAGIC, 8).add(Aspect.TOOL, 8).add(Aspect.ROCK, 8), -1, 5, "thaumcraft:wand_excavation")
            .setParents(researchUTFT)
            .registerResearchItem();
        researchWandTrade = new ResearchItem("WANDEXCHANGE", tags().add(Aspect.EXCHANGE, 16).add(Aspect.MAGIC, 8).add(Aspect.TOOL, 8), -1, 6, "thaumcraft:wand_equal_trade")
            .setParents(researchUTFT)
            .registerResearchItem();
        researchBootsTraveller = new ResearchItem("BOOTSTRAVELLER", tags().add(Aspect.MOTION, 24).add(Aspect.FLIGHT, 24).add(Aspect.ARMOR, 16).add(Aspect.EARTH, 16).add(Aspect.WATER, 16), 2, 10, "thaumcraft:boots_traveller")
            .setParents(researchUTFT)
            .setHidden()
            .registerResearchItem();
        researchTravelStone = new ResearchItem("TRAVELSTONE", tags().add(Aspect.MOTION, 16).add(Aspect.FLIGHT, 8).add(Aspect.EARTH, 16).add(Aspect.ROCK, 16), 4, 10, "thaumcraft:travel_paving_stone")
            .setParents(researchBootsTraveller)
            .setHidden()
            .registerResearchItem();
        researchElementalAxe = new ResearchItem("ELEMENTALAXE", tags().add(Aspect.WATER, 24).add(Aspect.TOOL, 24).add(Aspect.WOOD, 24).add(Aspect.MOTION, 8), 3, 5, "thaumcraft:elemental_axe")
            .setParents(researchWandFrost)
            .setHidden()
            .registerResearchItem();
        researchElementalSword = new ResearchItem("ELEMENTALSWORD", tags().add(Aspect.WIND, 24).add(Aspect.WEAPON, 24).add(Aspect.MOTION, 16).add(Aspect.DESTRUCTION, 16), 3, 6, "thaumcraft:elemental_sword")
            .setParents(researchWandLightning)
            .setHidden()
            .registerResearchItem();
        researchElementalPickAxe = new ResearchItem("ELEMENTALPICKAXE", tags().add(Aspect.FIRE, 24).add(Aspect.TOOL, 24).add(Aspect.METAL, 24).add(Aspect.VISION, 8), 3, 4, "thaumcraft:elemental_pickaxe")
            .setParents(researchWandFire)
            .setHidden()
            .registerResearchItem();
        researchElementalShovel = new ResearchItem("ELEMENTALSHOVEL", tags().add(Aspect.EARTH, 24).add(Aspect.TOOL, 24).add(Aspect.CRAFT, 16).add(Aspect.DESTRUCTION, 16), -3, 5, "thaumcraft:elemental_shovel")
            .setParents(researchWandExcavate)
            .setHidden()
            .registerResearchItem();
        researchElementalHoe = new ResearchItem("ELEMENTALHOE", tags().add(Aspect.PLANT, 24).add(Aspect.TOOL, 24).add(Aspect.CROP, 16).add(Aspect.LIFE, 16), -3, 6, "thaumcraft:elemental_hoe")
            .setParents(researchWandTrade)
            .setHidden()
            .registerResearchItem();
        researchGolemancy = new ResearchItem("GOLEMANCY", tags().add(Aspect.CONTROL, 16).add(Aspect.MOTION, 16).add(Aspect.SPIRIT, 8).add(Aspect.LIFE, 8), 3, 17, "thaumcraft:wood_golem")
            .setParents(researchUTFT)
            .setSpecial()
            .registerResearchItem();
        researchGolemFast = new ResearchItem("GOLEMFAST", tags().add(Aspect.CONTROL, 8).add(Aspect.EXCHANGE, 8).add(Aspect.MOTION, 16), 4, 19, "thaumcraft:golem_core_speed")
            .setParents(researchGolemancy)
            .registerResearchItem();
        researchGolemSmart = new ResearchItem("GOLEMSMART", tags().add(Aspect.CONTROL, 8).add(Aspect.EXCHANGE, 8).add(Aspect.KNOWLEDGE, 16), 4, 20, "thaumcraft:golem_core_intelligence")
            .setParents(researchGolemancy)
            .registerResearchItem();
        researchGolemSight = new ResearchItem("GOLEMSIGHT", tags().add(Aspect.CONTROL, 8).add(Aspect.EXCHANGE, 8).add(Aspect.VISION, 16), 2, 19, "thaumcraft:golem_core_perception")
            .setParents(researchGolemancy)
            .registerResearchItem();
        researchGolemStrong = new ResearchItem("GOLEMSTRONG", tags().add(Aspect.CONTROL, 8).add(Aspect.EXCHANGE, 8).add(Aspect.POWER, 16), 2, 20, "thaumcraft:golem_core_strength")
            .setParents(researchGolemancy)
            .registerResearchItem();
        researchGolemStone = new ResearchItem("GOLEMSTONE", tags().add(Aspect.ROCK, 16).add(Aspect.CONTROL, 4).add(Aspect.EXCHANGE, 8).add(Aspect.MOTION, 4), 3, 15, "thaumcraft:stone_golem")
            .setParents(researchGolemancy)
            .registerResearchItem();
        researchGolemClay = new ResearchItem("GOLEMCLAY", tags().add(Aspect.EARTH, 16).add(Aspect.CONTROL, 4).add(Aspect.EXCHANGE, 8).add(Aspect.MOTION, 4), 5, 15, "thaumcraft:clay_golem")
            .setParents(researchGolemStone)
            .registerResearchItem();
        researchGolemTallow = new ResearchItem("GOLEMTALLOW", tags().add(Aspect.FLESH, 8).add(Aspect.CRAFT, 8).add(Aspect.CONTROL, 4).add(Aspect.EXCHANGE, 8).add(Aspect.MOTION, 4), 7, 15, "thaumcraft:tallow_golem")
            .setParents(researchGolemClay, researchTallow)
            .setHidden()
            .registerResearchItem();
        researchGolemStraw = new ResearchItem("GOLEMSTRAW", tags()
                    .add(Aspect.CROP, 8)
                    .add(Aspect.PLANT, 8)
                    .add(Aspect.TOOL, 4)
                    .add(Aspect.CONTROL, 4)
                    .add(Aspect.EXCHANGE, 4)
                    .add(Aspect.MOTION, 4), 5, 17, "thaumcraft:straw_golem")
            .setParents(researchGolemancy)
            .registerResearchItem();
        researchInfernalFurnace = new ResearchItem("INFERNALFURNACE", tags()
                    .add(Aspect.FIRE, 16)
                    .add(Aspect.ROCK, 16)
                    .add(Aspect.CONTROL, 8)
                    .add(Aspect.TRAP, 8)
                    .add(Aspect.MAGIC, 8)
                    .add(Aspect.SPIRIT, 8), -2, 8, 2)
            .setParents(researchUTFT)
            .setHidden()
            .registerResearchItem();
        researchArcaneBellows = new ResearchItem("ARCANEBELLOWS", tags().add(Aspect.WIND, 16).add(Aspect.CONTROL, 4).add(Aspect.MOTION, 16).add(Aspect.TOOL, 8), -4, 8, "thaumcraft:arcane_bellows")
            .setParents(researchInfernalFurnace)
            .setHidden()
            .registerResearchItem();
        researchHungryChest = new ResearchItem("HUNGRYCHEST", tags().add(Aspect.VOID, 12).add(Aspect.MOTION, 8).add(Aspect.SPIRIT, 8).add(Aspect.EXCHANGE, 4), 1, 8, "thaumcraft:hungry_chest")
            .setParents(researchUTFT)
            .setHidden()
            .registerResearchItem();
        researchPortableHole = new ResearchItem("PORTABLEHOLE", tags().add(Aspect.VOID, 20).add(Aspect.MOTION, 8).add(Aspect.ELDRITCH, 8).add(Aspect.EXCHANGE, 8), 3, 8, "thaumcraft:portable_hole")
            .setParents(researchHungryChest)
            .setHidden()
            .registerResearchItem();
        researchMirror = new ResearchItem("MIRROR", tags()
                    .add(Aspect.VOID, 24)
                    .add(Aspect.MOTION, 24)
                    .add(Aspect.ELDRITCH, 24)
                    .add(Aspect.EXCHANGE, 24)
                    .add(Aspect.CRYSTAL, 16)
                    .add(Aspect.VISION, 16), 5, 8, "thaumcraft:magic_mirror")
            .setParents(researchPortableHole)
            .setHidden()
            .registerResearchItem();
        researchHandMirror = new ResearchItem("HANDMIRROR", tags()
                    .add(Aspect.VOID, 12)
                    .add(Aspect.MOTION, 12)
                    .add(Aspect.ELDRITCH, 12)
                    .add(Aspect.EXCHANGE, 12)
                    .add(Aspect.TOOL, 12)
                    .add(Aspect.VISION, 12), 5, 7, "thaumcraft:hand_mirror")
            .setParents(researchMirror)
            .setHidden()
            .registerResearchItem();
        researchJar = new ResearchItem("JAR", tags().add(Aspect.CRYSTAL, 16).add(Aspect.TRAP, 16).add(Aspect.VOID, 16).add(Aspect.ARMOR, 8), -2, 16, "thaumcraft:warded_jar")
            .setParents(researchUTFT)
            .registerResearchItem();
        researchJarBrain = new ResearchItem("JARBRAIN", tags().add(Aspect.EVIL, 16).add(Aspect.SPIRIT, 16).add(Aspect.KNOWLEDGE, 16).add(Aspect.EXCHANGE, 16), -2, 14, "thaumcraft:brain_jar")
            .setParents(researchJar)
            .setHidden()
            .registerResearchItem();
        researchBasicFlux = new ResearchItem("BASICFLUX", tags().add(Aspect.FLUX, 20).add(Aspect.CONTROL, 8).add(Aspect.EXCHANGE, 8).add(Aspect.PURE, 8).add(Aspect.MECHANISM, 4), -2, 18, 3)
            .setParents(researchJar)
            .registerResearchItem();
        researchArcaneDoor = new ResearchItem("ARCANEDOOR", tags().add(Aspect.MOTION, 8).add(Aspect.CONTROL, 8).add(Aspect.MECHANISM, 8).add(Aspect.KNOWLEDGE, 8).add(Aspect.ARMOR, 8), 2, 12, "thaumcraft:arcane_door")
            .setParents(researchUTFT)
            .setHidden()
            .registerResearchItem();
        researchArcanePlate = new ResearchItem("ARCANEPLATE", tags().add(Aspect.POWER, 8).add(Aspect.CONTROL, 8).add(Aspect.MECHANISM, 16).add(Aspect.KNOWLEDGE, 8).add(Aspect.ARMOR, 8), 4, 12, "thaumcraft:arcane_pressure_plate")
            .setParents(researchArcaneDoor)
            .setHidden()
            .registerResearchItem();
        researchLevitator = new ResearchItem("LEVITATOR", tags().add(Aspect.MOTION, 12).add(Aspect.FLIGHT, 16).add(Aspect.MECHANISM, 16).add(Aspect.EARTH, 12).add(Aspect.WIND, 12), -2, 10, "thaumcraft:arcane_levitator")
            .setParents(researchUTFT)
            .setHidden()
            .registerResearchItem();
        researchArcaneEar = new ResearchItem("ARCANEEAR", tags().add(Aspect.SOUND, 24).add(Aspect.WIND, 12).add(Aspect.MECHANISM, 12).add(Aspect.VISION, 8).add(Aspect.CONTROL, 12), -2, 12, "thaumcraft:arcane_ear")
            .setParents(researchUTFT)
            .setHidden()
            .registerResearchItem();
        researchCrystalCluster = new ResearchItem("CRYSTALCLUSTER", tags().add(Aspect.CRYSTAL, 24).add(Aspect.MAGIC, 32).add(Aspect.EXCHANGE, 24), -4, 18, "thaumcraft:mixed_crystal_cluster")
            .setParents(researchBasicFlux)
            .registerResearchItem();
        researchGolemStoneAdv = new ResearchItem("GOLEMSTONEADV", tags().add(Aspect.KNOWLEDGE, 24).add(Aspect.CONTROL, 16).add(Aspect.EXCHANGE, 16).add(Aspect.PURE, 8).add(Aspect.SPIRIT, 8), 3, 14, "thaumcraft:advanced_stone_golem")
            .setHidden()
            .setParents(researchGolemStone, researchJarBrain)
            .registerResearchItem();
        researchGolemClayAdv = new ResearchItem("GOLEMCLAYADV", tags().add(Aspect.KNOWLEDGE, 24).add(Aspect.CONTROL, 16).add(Aspect.EXCHANGE, 16).add(Aspect.PURE, 8).add(Aspect.SPIRIT, 8), 5, 14, "thaumcraft:advanced_clay_golem")
            .setHidden()
            .setParents(researchGolemClay, researchJarBrain)
            .registerResearchItem();
        researchGolemTallowAdv = new ResearchItem("GOLEMTALLOWADV", tags().add(Aspect.CONTROL, 16).add(Aspect.WATER, 16).add(Aspect.KNOWLEDGE, 24).add(Aspect.MOTION, 8).add(Aspect.PURE, 8), 8, 15, "thaumcraft:decanting_golem")
            .setParents(researchGolemTallow, researchJarBrain)
            .setHidden()
            .registerResearchItem();
        researchTinyHat = new ResearchItem("TINYHAT", tags().add(Aspect.CLOTH, 24).add(Aspect.VALUABLE, 24).add(Aspect.POWER, 24).add(Aspect.KNOWLEDGE, 24), 6, 19, "thaumcraft:golem_top_hat")
            .setHidden()
            .setLost()
            .registerResearchItem();
        researchTinyFez = new ResearchItem("TINYFEZ", tags().add(Aspect.CLOTH, 24).add(Aspect.VALUABLE, 24).add(Aspect.LIFE, 24).add(Aspect.KNOWLEDGE, 24), 7, 20, "thaumcraft:golem_fez")
            .setHidden()
            .setLost()
            .registerResearchItem();
        researchTinyGlasses = new ResearchItem("TINYGLASSES", tags().add(Aspect.CRYSTAL, 24).add(Aspect.VALUABLE, 24).add(Aspect.VISION, 24).add(Aspect.KNOWLEDGE, 24), 7, 19, "thaumcraft:golem_spectacles")
            .setHidden()
            .setLost()
            .registerResearchItem();
        researchTinyBowTie = new ResearchItem("TINYBOWTIE", tags().add(Aspect.CLOTH, 24).add(Aspect.VALUABLE, 24).add(Aspect.MOTION, 24).add(Aspect.KNOWLEDGE, 24), 6, 20, "thaumcraft:golem_bowtie")
            .setHidden()
            .setLost()
            .registerResearchItem();
        researchTinyDart = new ResearchItem("TINYDART", tags().add(Aspect.WEAPON, 24).add(Aspect.WIND, 24).add(Aspect.MECHANISM, 24).add(Aspect.KNOWLEDGE, 24), 6, 22, "thaumcraft:golem_dart_launcher")
            .setHidden()
            .setLost()
            .registerResearchItem();
        researchTinyVisor = new ResearchItem("TINYVISOR", tags().add(Aspect.ARMOR, 24).add(Aspect.DEATH, 24).add(Aspect.VISION, 24).add(Aspect.KNOWLEDGE, 24), 7, 22, "thaumcraft:golem_visor")
            .setHidden()
            .setLost()
            .registerResearchItem();
        researchTinyArmor = new ResearchItem("TINYARMOR", tags().add(Aspect.ARMOR, 36).add(Aspect.METAL, 36).add(Aspect.KNOWLEDGE, 24), 8, 22, "thaumcraft:golem_iron_plating")
            .setHidden()
            .setLost()
            .registerResearchItem();
        tags();
        researchTTOE = new ResearchItem("TTOE", tags()
                    .add(Aspect.KNOWLEDGE, 24)
                    .add(Aspect.ARMOR, 8)
                    .add(Aspect.INSECT, 8)
                    .add(Aspect.PLANT, 8)
                    .add(Aspect.WEAPON, 8)
                    .add(Aspect.BEAST, 8)
                    .add(Aspect.FLESH, 8)
                    .add(Aspect.LIFE, 8)
                    .add(Aspect.POISON, 8)
                    .add(Aspect.WOOD, 8)
                    .add(Aspect.CROP, 8)
                    .add(Aspect.FLOWER, 8)
                    .add(Aspect.MECHANISM, 8)
                    .add(Aspect.SOUND, 8)
                    .add(Aspect.CRYSTAL, 8)
                    .add(Aspect.FUNGUS, 8)
                    .add(Aspect.METAL, 8)
                    .add(Aspect.TOOL, 8), 0, 22, 1)
            .setSpecial()
            .setParents(researchUTFT, researchGolemancy, researchBasicFlux)
            .registerResearchItem();
        researchCrystalCore = new ResearchItem("CRYSTALCORE", tags()
                    .add(Aspect.MAGIC, 32)
                    .add(Aspect.CONTROL, 32)
                    .add(Aspect.EXCHANGE, 24)
                    .add(Aspect.FLUX, 16)
                    .add(Aspect.MOTION, 32)
                    .add(Aspect.VOID, 16), -4, 23, "thaumcraft:crystal_core")
            .setParents(researchTTOE, researchCrystalCluster)
            .registerResearchItem();
        researchCrystalCapacitor = new ResearchItem("CRYSTALCAPACITOR", tags().add(Aspect.CRYSTAL, 24).add(Aspect.MAGIC, 32).add(Aspect.EXCHANGE, 24), -6, 23, "thaumcraft:crystal_capacitor")
            .setParents(researchCrystalCore)
            .setHidden()
            .registerResearchItem();
        researchArcaneBore = new ResearchItem("ARCANEBORE", tags()
                    .add(Aspect.METAL, 32)
                    .add(Aspect.EARTH, 32)
                    .add(Aspect.ROCK, 32)
                    .add(Aspect.MECHANISM, 32)
                    .add(Aspect.MOTION, 24)
                    .add(Aspect.TOOL, 32)
                    .add(Aspect.POWER, 24)
                    .add(Aspect.VOID, 24), -2, 25, "thaumcraft:arcane_bore")
            .setParents(researchTTOE)
            .setParentsHidden(researchElementalPickAxe, researchWandExcavate, researchPortableHole)
            .registerResearchItem();
        researchGolemIronGuardian = new ResearchItem("GOLEMANCYADV", tags()
                    .add(Aspect.CONTROL, 16)
                    .add(Aspect.MOTION, 16)
                    .add(Aspect.SPIRIT, 16)
                    .add(Aspect.LIFE, 16)
                    .add(Aspect.ARMOR, 16)
                    .add(Aspect.WEAPON, 16), 2, 24, "thaumcraft:iron_guardian_golem")
            .setParents(researchTTOE)
            .setSpecial()
            .registerResearchItem();
        researchHellrod = new ResearchItem("HELLROD", tags().add(Aspect.FIRE, 24).add(Aspect.MAGIC, 16).add(Aspect.WEAPON, 16).add(Aspect.BEAST, 24).add(Aspect.EVIL, 24), 2, 26, "thaumcraft:hellrod")
            .setParents(researchTTOE)
            .setParentsHidden(researchWandFire)
            .setHidden()
            .registerResearchItem();
        researchHoverHarness = new ResearchItem("HOVERHARNESS", tags()
                    .add(Aspect.FLIGHT, 48)
                    .add(Aspect.WIND, 32)
                    .add(Aspect.MOTION, 24)
                    .add(Aspect.POWER, 32)
                    .add(Aspect.CONTROL, 16)
                    .add(Aspect.ARMOR, 8), -2, 27, "thaumcraft:hover_harness")
            .setParents(researchTTOE)
            .setParentsHidden(researchJar, researchLevitator)
            .setHidden()
            .registerResearchItem();
    
    }
}
