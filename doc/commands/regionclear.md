# Regionclear Command

## Description

This command manages the regions' clearing behaviour by adding/removing exceptions.

## Usage Examples

Command |  Definition
------------- | -------------
/pa ctf regionclear VILLAGER | toggle the VILLAGER clearing of the arena "ctf"
/pa ctf !rc TNT true       | enable the TNT clearing exception of the arena "ctf" (ALLOW TNT to stay after clearing!)

## Details

Valid entitytypes will be told when you mistype, or you can find them here:

https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html

Giving no second argument will just toggle exception status and show you the result, valid arguments to activate/deactivate include:

on | 1 | true || off | 0 | false
