# Upgrading from 1.3.x

If you want to upgrade your arena configurations to PVPArena 1.13 version or above, you have
to update two things : Item Lists and Schematics

## Item lists

Probably the worst thing to upgrade, just clear the inventories of your arena classes and
re-create them. Be also careful to remove items list of pvparena modules like **chestfiller** 
or **blockdissolve**.

## WorldEdit

You have to remove Worldedit schematics too. A big part of blocks will not be pasted if you
keep it as it is and chest orientation will be broken.

So just remove your schematics files and re-create them with `/pa [arena] regsave [regionname]`


## Miscellaneous

**In "flags" goal :** Just replace `WOOL` block name by `WHITE_WOOL`. The color will be
automatically defined with the color of the team.