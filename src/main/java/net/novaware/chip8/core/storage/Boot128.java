/*
 * Copyright 1998-2019 David Winter @ http://www.pong-story.com/chip8/
 *
 * Licensed under the POSTWARE license.
 *
 * A POSTWARE is like a FREEWARE, except that you have to send an email to the
 * author if you decide to use this software. The minimum you have say is who
 * you are. You can also add something else like what you think about this
 * software, how much you use it, your own appreciations and suggestions, etc.
 *
 * The rules of the FREEWARE:
 *  - You MUST obtain your copy FREELY,
 *  - You can give it ONLY IN ITS ORIGINAL INTEGRITY: you MUST NOT ADD, ERASE,
 *    or MODIFY any of its files,
 *  - You MUST NOT SELL it,
 *  - You MUST NOT distribute it for any charge excepted:
 *     * The shipping charges,
 *     * The price of the media (diskette, tape...) of THE copy you GIVE,
 * If you require money to distribute it, you MUST obtain author's express
 * written permission.
 * This software may be distributed on CD-ROM, diskette, tape or free server
 * ONLY if you agree with this entire section.
 */
package net.novaware.chip8.core.storage;

/**
 * Memory editing app loaded by CHIP8 Emulator if no other ROM was provided.
 *
 * @author dwinter David Winter
 */
interface Boot128 {
    int MEMORY_START = 0x100;
    int[] CODE = new int[]{
            0x0001, 0x6302, 0x3500, 0x1124, 0xF329, 0x2170, 0x8040, 0x8006,
            0x8006, 0x8006, 0x8006, 0xF029, 0x2170, 0xF429, 0x2170, 0xA176,
            0x2170, 0x75FF, 0x2166, 0x8780, 0x870E, 0x870E, 0x870E, 0x870E,
            0x2166, 0x8874, 0xA000, 0xF41E, 0x8030, 0x6180, 0xF11E, 0xF11E,
            0x70FF, 0x3000, 0x113C, 0x8080, 0xF055, 0x7401, 0x4400, 0x7301,
            0x4310, 0x1200, 0x3527, 0x1104, 0x6500, 0x4618, 0x1162, 0x7606,
            0x1104, 0x0000, 0x1104, 0xA17A, 0xD566, 0xF80A, 0xD566, 0xF829,
            0xD565, 0x7505, 0x00EE, 0x0040, 0x0040, 0x0000, 0x0000, 0x00F0
    };
}
