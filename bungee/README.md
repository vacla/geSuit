geSuit - Bungeecord plugin 
===
Plugin suite for BungeeCord

Building 
---
Clone this repo, cd into it and run

    mvn package

The jar file will be inside the `target` folder

Installing:
---
Builds for this plugin may be found [here](http://jenkins.addstar.com.au/job/geSuit), or you can [compile it yourself](#building)

####1.13
The 1.0 Version is targetted for 1.13 servers please use 0.9.x builds for 1.12

Requires [Yamler](https://www.spigotmc.org/resources/yamler.315/) version 2.4 and a MySQL server

* Place geSuit.jar (and Yamler) inside your bungee's _plugins/_ folder, and restart BungeeCord.
* Fill in your MySQL server's information in config.yml (inside the _geSuit/_ folder)
* Configure anything else you want in the files in the _geSuit/_ folder
* Give the players permission to use the commands
* Done!

Additional features:
---

The following Bukkit / Spigot plugins are optional, and require the base geSuit to function.

* [Homes](https://github.com/AddstarMC/geSuitHomes)
* [Bans](https://github.com/AddstarMC/geSuitBans)
* [Teleportation](https://github.com/AddstarMC/geSuitTeleport)
* [Spawn](https://github.com/AddstarMC/geSuitSpawn)
* [Portals](https://github.com/AddstarMC/geSuitPortals)
* [Warps](https://github.com/AddstarMC/geSuitWarps)
* [Admin](https://github.com/AddstarMC/geSuitAdmin)

Additonal geSuit supports the use of 

 * [BStats](https://bstats.org/plugin/bungeecord/geSuit) - this reports plugin usage so developers have an idea of usage
 * [DripReporterApi](https://jenkins.addstar.com.au/job/DripMaster/au.com.addstar.dripreporter$DripReporterAPI/)  - this reports server health metrics in a  very low impact method 
    a number of methods of reporting those metrics.  It requires you to install 
   * [DripCordReporter](https://jenkins.addstar.com.au/job/DripMaster/au.com.addstar.dripreporter$DripCordReporter/) on BungeeCord
     
Integration Tests:
---
Requirements:
- Mysql server - with a user:root password:password and a db:gesuit.