# TekTopiaPlus

A major fork, rework, and expansion of the [TektopiaAddons](https://github.com/Sushiy/TektopiaAddons) Minecraft mod.

---

## 📖 About

**TekTopiaPlus** is a modernized and extended rework of the original **TektopiaAddons** mod.  
It focuses on:

- ✅ Bugfixes and stability improvements
- ✅ Community-requested features
- ✅ Improved compatibility with third-party mods

This project is a fork of **[TektopiaAddons](https://github.com/Sushiy/TektopiaAddons)** by [Sushiy](https://github.com/Sushiy).

---

## 🛠️ Bugfixes

- Fixed miner coal collection and proper AI filter registration
- Corrected farmer behavior with beetroot and sugar cane harvesting
- Improved compatibility with third-party food mods (e.g. *Pam's HarvestCraft*)
- Fixed an issue where modded ores had an inconsistent drop rate when mined - They now drop reliably, consistent with vanilla ores

---

## ✨ New Features

- Villager stone resource usage
- Enhanced compatibility with third-party food mods via reworked food registration logic
- Refactored and cleaned open-source code (removed unnecessary files) *(50% done)*

---

## ⚠️ Current Issues

- Miner does not farm third-party mod ores that are not directly in front of them
- Ore-Respawn through Druids is broken for modded ores; they do not respawn in mine shafts as intended
- A system to allow manual addition of ores from other mods via config is required
- Ensuring all magical ores from other mods are correctly identified and collected
- Miner does not use third-party mod tools
- Lumberjack does not use third-party mod tools

*(Contributions and pull requests are welcome!)*

---

## 🔗 Tested Compatibility

- **TekTopia** – Version 1.1.0
- **Pam's HarvestCraft** – Version 1.12.2zg
- **Aquaculture** – Version 1.6.8
- **Thermal Foundation** – Version 2.6.7.1
- **TinkersAntique** _(Only Ores)_ – Version 2.13.0.202

---

## 🙌 Credits

- **[XMxmx111](https://github.com/XMxmx111)** – for bugfix contributions
- **limited** – for intensive testing and bug hunting

---

## ⚖️ License

This project is licensed under the **MIT License**.