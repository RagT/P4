/**
 * Cache.java
 */
public class Cache {
    private CacheBlock[] blocks; //Array of blocks in cache
    private int blockSize;  //Size of each block in cache

    //Class used to store cache block information
    private class CacheBlock {
        public byte[] data;
        public boolean dirty;  //dirty bit
        public boolean reference;  //reference bit
        public int frame; //blockFrame

        private CacheBlock (int blockSize){
            data = new byte[blockSize];
            dirty = false;
            reference = false;
            frame = -1;         //Set to default empty value
        }
    }

    /*
    The constructor: allocates a cacheBlocks number of cache blocks,
    each containing blockSize-byte data, on memory
     */
    public Cache( int blockSize, int cacheBlocks ) {
        blocks = new CacheBlock[cacheBlocks];
        this.blockSize = blockSize;
        for(int i = 0; i < cacheBlocks; i++) {
            blocks[i] = new CacheBlock(blockSize);
        }
    }

    /*
    reads into the buffer[ ] array the cache block specified by blockId from the disk cache if it is in cache,
    otherwise reads the corresponding disk block from the disk device.
    Upon an error, it should return false, otherwise return true.
     */
    public boolean read( int blockId, byte buffer[ ] ) {
        if(blockId < 0) {   //Invalid block id
            return false;
        }
        int blockFound = findBlock(blockId);
        if(blockFound >= 0){ //Block found in cache
            readFromCache(blockFound, blockId, buffer);
            return true;
        }

        //If cannot find a block with given Id
        blockFound = findBlock(-1); //Get first available empty block in the cache
        if(blockFound > 0) {
            writeToCache(blockFound, blockId, buffer); //Assign blockId to first available block
            return true;
        }

        //If cache full, need to choose victim block to replace
        int victim = getVictim();
        writeToDisk(victim); //Writes victim to disk if dirty bit is set
        SysLib.rawread(blockId, blocks[victim].data); //Reads new item from disk to replace victim
        readFromCache(blockFound, blockId, buffer);
        return true;
    }

    /*
    writes the buffer[ ]array contents to the cache block specified by blockId from the disk cache if it is in cache,
    otherwise finds a free cache block and writes the buffer [ ] contents on it.
     No write through. Upon an error, it should return false, otherwise return true.
     */
    public boolean write( int blockId, byte buffer[ ] ){
        if(blockId < 0) {   //Invalid block id
            return false;
        }
        int blockFound = findBlock(blockId);
        if(blockFound >= 0){ //Block found in cache
            writeToCache(blockFound, blockId, buffer);
            return true;
        }

        //If cannot find a block with given Id
        blockFound = findBlock(-1); //Get first available empty block in the cache
        if(blockFound > 0) {
            writeToCache(blockFound, blockId, buffer); //Assign blockId to first available block
            return true;
        }

        //If cache full, need to choose victim block to replace
        int victim = getVictim();
        writeToDisk(victim); //Writes victim to disk if dirty bit is set
        SysLib.rawread(blockId, blocks[victim].data); //Reads new item from disk to replace victim
        writeToCache(blockFound, blockId, buffer);
        return true;
    }

    //Writes back all dirty blocks to Disk
    public synchronized void sync( ){
        for(int i = 0; i < blocks.length; i++) {
            writeToDisk(i);
        }
        SysLib.sync();
    }

    //Clears cache and writes all dirty blocks to disk
    //Used when shutting down ThreadOS
    public synchronized void flush(){
        for(int i = 0; i < blocks.length; i++) {
            writeToDisk(i);
            update(i, -1, false); //Invalidate block
        }
        SysLib.sync();
    }

    /*
    Returns the location of the block with the frameId specified.
    If block not found returns -1;
     */
    public int findBlock(int frameId) {
        for(int i = 0; i < blocks.length; i++) {
            if(blocks[i].frame == frameId) {
                return i;
            }
        }
        return -1;
    }

    //Reads block from cache to buffer
    private void readFromCache(int foundLoc, int blockId, byte buffer[]) {
        System.arraycopy(blocks[foundLoc].data, 0, buffer, 0, blockSize); //Copy data to buffer
        update(foundLoc, blockId, true); //Set reference bit
    }

    //Writes data to cache and reads it to buffer
    private void writeToCache(int foundLoc, int blockId, byte buffer[]) {
        System.arraycopy(blocks[foundLoc].data, 0, buffer, 0, blockSize); //Copy data to buffer
        blocks[foundLoc].dirty = true; //Set dirty bit of block we are writing to
        update(foundLoc, blockId, true); //Sets reference bit
    }

    //If dirty bit is set, write block to disk
    public void writeToDisk(int writeIndex) {
        if(blocks[writeIndex].dirty && blocks[writeIndex].frame != -1)
        {
            SysLib.rawwrite(blocks[writeIndex].frame, blocks[writeIndex].data);
            blocks[writeIndex].dirty = false;     //update dirty bit
        }
    }

    //Utility function to change frame and reference bit values in a specific cache block
    public void update(int index, int frame, boolean ref){
        blocks[index].frame = frame;
        blocks[index].reference = ref;
    }

    //Picks a block as a victim
    public int getVictim() {
        for(int victim = 0; true ; victim = (victim + 1) % blocks.length){
            if(!blocks[victim].reference) { //If reference is false dirty bit must also be false
                return victim;
            }
            blocks[victim].reference = false; //Unset reference bit (if unreferenced block not found,
                                              //Switches to a fifo order
        }
    }
}
