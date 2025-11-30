
---

# Sound Blocks

### A configurable ambient audio block for Fabric (Minecraft 1.20.1)

**Sound Blocks** is a Fabric mod that introduces a fully configurable block capable of emitting custom ambient sounds.
The mod is designed for builders, mapmakers, modpack authors, and server administrators who need fine-grained control over environmental audio.

It provides per-block audio customization, persistent data storage, and a clean configuration interface that integrates naturally into the Minecraft experience.

This mod was entirely made by A.I, both ChatGPT 5.1 and JetBrains Gemini 3 Pro in one day.

---

## Features

### Custom Sound Events

Each Sound Block can play any valid Minecraft sound event, including:

* Vanilla sounds
* Sounds added by other mods
* Sounds supplied through external resource packs

Users may enter any sound ID in the standard `namespace:path` format.

---

### Per-Block Configuration

Each block instance stores its own configuration and allows tuning of:

* Volume
* Pitch
* Audible range (distance attenuation)
* Loop mode
* Selected sound event

All values are saved to NBT and persist across world loads.

---

### Configuration Interface

Right-clicking the block opens a dedicated configuration screen.
The interface includes:

* A text field for sound IDs
* Sliders for volume, pitch, and range
* A toggle for looping
* A button for previewing the selected sound locally

Inventory slots are hidden to provide a focused and unobstructed layout.

---

### Audio System

The mod uses Minecraft’s client audio engine to manage playback.
Features include:

* Looping playback for ambient sounds
* Distance-based attenuation
* Automatic stopping when the block is removed or unloaded
* Local preview playback from the configuration screen

The system remains lightweight and does not modify global audio behaviour.

---

## Technical Structure

### Block and BlockEntity

The Sound Block extends `BaseEntityBlock` and instantiates a custom `BlockEntity` to store configuration values.
The BlockEntity handles:

* NBT serialization
* Synchronization with the client
* Providing the menu interface

### GUI and Menu Logic

The mod defines a custom `ScreenHandler` for server-authoritative configuration, paired with a dedicated client-side screen responsible for rendering widgets and interacting with user input.

Data is synchronized safely between client and server using Fabric's networking APIs.

### Requirements

* Fabric Loader (1.20.1)
* Fabric API
* Java 21
* Works with Mojang official mappings

---

## License

This project can be distributed under the MIT License.

---
