package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Packet51MapChunk extends Packet {

    public int a;
    public int b;
    public int c;
    public int d;
    // CraftBukkit start - private -> public
    public byte[] buffer;
    public byte[] inflatedBuffer;
    public boolean e;
    public int size;
    // CraftBukkit end
    private static byte[] buildBuffer = new byte[196864];

    public Packet51MapChunk() {
        this.lowPriority = true;
    }

    public Packet51MapChunk(Chunk chunk, boolean flag, int i) {
        this.lowPriority = true;
        this.a = chunk.x;
        this.b = chunk.z;
        this.e = flag;
        ChunkMap chunkmap = a(chunk, flag, i);
        // Deflater deflater = new Deflater(-1); // CraftBukkit

        this.d = chunkmap.c;
        this.c = chunkmap.b;

        /* CraftBukkit start - compression moved to new thread
        try {
            this.inflatedBuffer = chunkmap.a;
            deflater.setInput(chunkmap.a, 0, chunkmap.a.length);
            deflater.finish();
            this.buffer = new byte[chunkmap.a.length];
            this.size = deflater.deflate(this.buffer);
        } finally {
            deflater.end();
        }
        */
        this.inflatedBuffer = chunkmap.a;
        // CraftBukkit end
    }

    public void a(DataInputStream datainputstream) throws IOException {
        this.a = datainputstream.readInt();
        this.b = datainputstream.readInt();
        this.e = datainputstream.readBoolean();
        this.c = datainputstream.readShort();
        this.d = datainputstream.readShort();
        this.size = datainputstream.readInt();
        if (buildBuffer.length < this.size) {
            buildBuffer = new byte[this.size];
        }

        datainputstream.readFully(buildBuffer, 0, this.size);
        int i = 0;

        int j;

        for (j = 0; j < 16; ++j) {
            i += this.c >> j & 1;
        }

        j = 12288 * i;
        if (this.e) {
            j += 256;
        }

        this.inflatedBuffer = new byte[j];
        Inflater inflater = new Inflater();

        inflater.setInput(buildBuffer, 0, this.size);

        try {
            inflater.inflate(this.inflatedBuffer);
        } catch (DataFormatException dataformatexception) {
            throw new IOException("Bad compressed data format");
        } finally {
            inflater.end();
        }
    }

    public void a(DataOutputStream dataoutputstream) throws IOException { // CraftBukkit - throws IOException
        dataoutputstream.writeInt(this.a);
        dataoutputstream.writeInt(this.b);
        dataoutputstream.writeBoolean(this.e);
        dataoutputstream.writeShort((short) (this.c & '\uffff'));
        dataoutputstream.writeShort((short) (this.d & '\uffff'));
        dataoutputstream.writeInt(this.size);
        dataoutputstream.write(this.buffer, 0, this.size);
    }

    public void handle(NetHandler nethandler) {
        nethandler.a(this);
    }

    public int a() {
        return 17 + this.size;
    }

    public static ChunkMap a(Chunk chunk, boolean flag, int i) {
        // begin AntiXray
        int[] sections = new int[16];
        int numSections = 0;
        // end AntiXray

        int j = 0;
        ChunkSection[] achunksection = chunk.i();
        int k = 0;
        ChunkMap chunkmap = new ChunkMap();
        byte[] abyte = buildBuffer;

        if (flag) {
            chunk.seenByPlayer = true;
        }

        int l;

        for (l = 0; l < achunksection.length; ++l) {
            if (achunksection[l] != null && (!flag || !achunksection[l].a()) && (i & 1 << l) != 0) {
                chunkmap.b |= 1 << l;
                sections[numSections++] = l; // AntiXray
                if (achunksection[l].i() != null) {
                    chunkmap.c |= 1 << l;
                    ++k;
                }
            }
        }

        for (l = 0; l < achunksection.length; ++l) {
            if (achunksection[l] != null && (!flag || !achunksection[l].a()) && (i & 1 << l) != 0) {
                byte[] abyte1 = achunksection[l].g();

                System.arraycopy(abyte1, 0, abyte, j, abyte1.length);
                j += abyte1.length;
            }
        }
        int endBlockPos = j;  // AntiXray

        NibbleArray nibblearray;

        for (l = 0; l < achunksection.length; ++l) {
            if (achunksection[l] != null && (!flag || !achunksection[l].a()) && (i & 1 << l) != 0) {
                nibblearray = achunksection[l].j();
                System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
                j += nibblearray.a.length;
            }
        }

        for (l = 0; l < achunksection.length; ++l) {
            if (achunksection[l] != null && (!flag || !achunksection[l].a()) && (i & 1 << l) != 0) {
                nibblearray = achunksection[l].k();
                System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
                j += nibblearray.a.length;
            }
        }

        for (l = 0; l < achunksection.length; ++l) {
            if (achunksection[l] != null && (!flag || !achunksection[l].a()) && (i & 1 << l) != 0) {
                nibblearray = achunksection[l].l();
                System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
                j += nibblearray.a.length;
            }
        }

        if (k > 0) {
            for (l = 0; l < achunksection.length; ++l) {
                if (achunksection[l] != null && (!flag || !achunksection[l].a()) && achunksection[l].i() != null && (i & 1 << l) != 0) {
                    nibblearray = achunksection[l].i();
                    System.arraycopy(nibblearray.a, 0, abyte, j, nibblearray.a.length);
                    j += nibblearray.a.length;
                }
            }
        }

        if (flag) {
            byte[] abyte2 = chunk.m();

            System.arraycopy(abyte2, 0, abyte, j, abyte2.length);
            j += abyte2.length;
        }

        chunkmap.a = new byte[j];
        System.arraycopy(abyte, 0, chunkmap.a, 0, j);

        // begin AntiXray
        byte disguise = 1;
        for (int q = 0; q < endBlockPos; q++) {
            if (!checkProtectedBlock(chunkmap.a[q])) {
                continue;
            }

            int s = (int)(q / 4096);
            int section = sections[s];
            int sectionY = (int)(q / 256) - (s * 16);
            int y = sectionY + (s * 16);
            int z = (int)(((q % 4096) % 256) / 16);
            int x = (int)(((q % 4096) % 256) % 16);

            // check down, ignore y = 0
            if (y > 0) {
                // don't check under bottom section
                if (sectionY == 0) {
                    // if nonexisting section below, it's air - no disguise
                    if (s >= 1 && (section - 1) != sections[s - 1]) {
                        continue;
                    }
                }
                // if the block below is see thru - no disguise
                byte downId = chunkmap.a[q - 256];
                if (isSeeThru(downId)) {
                    continue;
                }
            }

            // check up, ignore y = 255
            if (y < 255) {
                // don't check over top section
                if (section == 15) {
                    // if last section, then air above - no disguise
                    if (s == (numSections - 1)) {
                        continue;
                    }
                    // if nonexisting section above, it's air - no disguise
                    if ((section + 1) != sections[s + 1]) {
                        continue;
                    }
                }
                // if the block above is see thru - no disguise
                byte upId = chunkmap.a[q + 256];
                if (isSeeThru(upId)) {
                    continue;
                }
            }

            // check north block
            byte northId;
            if (x == 0) {
                // at northern edge of chunk
                // note: adjacent z is 15
                northId = getAdjacentId(chunk.world, chunk.x - 1, chunk.z, section, sectionY, z, 15);
                if (northId == 0) {
                    // no section, so it's air - no disguise
                    continue;
                }
            } else {
                // within current chunk
                northId = chunkmap.a[q - 1];
            }
            // if the northern block is see thru - no disguise
            if (isSeeThru(northId)) {
                continue;
            }

            // check south block
            byte southId;
            if (x == 15) {
                // at southern edge of chunk
                // note: adjacent z is 0
                southId = getAdjacentId(chunk.world, chunk.x + 1, chunk.z, section, sectionY, z, 0);
                if (southId == 0) {
                    // no section, so it's air - no disguise
                    continue;
                }
            } else {
                // within current chunk
                southId = chunkmap.a[q + 1];
            }
            // if the southern block is see thru - no disguise
            if (isSeeThru(southId)) {
                continue;
            }

            // check west block
            byte westId;
            if (z == 15) {
                // at western edge of chunk
                // note: adjacent x is 0
                westId = getAdjacentId(chunk.world, chunk.x, chunk.z + 1, section, sectionY, 0, x);
                if (westId == 0) {
                    // no section, so it's air - no disguise
                    continue;
                }
            } else {
                // within current chunk
                westId = chunkmap.a[q + 16];
            }
            // if the western block is see thru - no disguise
            if (isSeeThru(westId)) {
                continue;
            }

            // check east block
            byte eastId;
            if (z == 0) {
                // at eastern edge of chunk
                // note: adjacent x is 15
                eastId = getAdjacentId(chunk.world, chunk.x, chunk.z - 1, section, sectionY, 15, x);
                if (eastId == 0) {
                    // no section, so it's air - no disguise
                    continue;
                }
            } else {
                // within current chunk
                eastId = chunkmap.a[q - 16];
            }
            // if the eastern block is see thru - no disguise
            if (isSeeThru(eastId)) {
                continue;
            }

            // ok, now we can disguise it
            chunkmap.a[q] = disguise;
        }
        // end AntiXray

        return chunkmap;
    }

    // get section from adjacent chunk
    private static byte getAdjacentId(World world, int worldX, int worldZ, int section, int y, int z, int x) {
        Chunk adjChunk = world.getChunkAt(worldX, worldZ);
        ChunkSection[] adjSections = adjChunk.i();
        ChunkSection adjSection = adjSections[section];

        if (adjSection == null) {
            return 0;
        }

        int adjIndex = (y * 256) + (z * 16) + x;
        byte[] adjBlocks = adjSection.g();
        return adjBlocks[adjIndex];
    }

    // check if block is protected
    private static boolean checkProtectedBlock(byte id) {
        // to get the byte for ids 128 and above, substract 256
        byte[] ids = { 14, 15, 16, 21, 48, 54, 56, 73, 74, 98, -127 };

        for (int i = 0; i < ids.length; i++) {
            if (id == ids[i]) {
                return true;
            }
        }

        return false;
    }

    // check if block is transparent or partly see thru
    private static boolean isSeeThru(byte id) {
        // to get the byte for ids 128 and above, substract 256
        byte[] ids = { 0, 8, 9, 18, 20, 26, 27, 28, 30, 31, 32, 34, 37, 38, 39, 40, 44, 50, 51, 53, 55, 59, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 75, 76, 77, 79, 83, 85, 90, 92, 93, 94, 96, 101, 102, 104, 105, 106, 107, 109, 111, 113, 114, 115, 117, 118, 119, 122, 126, 127, -128, -125, -124, -122, -121, -120 };

        for (int i = 0; i < ids.length; i++) {
            if (id == ids[i]) {
                return true;
            }
        }

        return false;
    }
}
