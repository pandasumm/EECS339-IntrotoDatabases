package simpledb;

/** Unique identifier for HeapPage objects. */
public class HeapPageId implements PageId {

    /**
     * Constructor. Create a page id structure for a specific page of a
     * specific table.
     *
     * @param tableId The table that is being referenced
     * @param pgNo The page number in that table.
     */
	private int tableID;
	private int pageNumber;
    public HeapPageId(int tableId, int pgNo) {
        // some code goes here
    	this.pageNumber = pgNo;
    	this.tableID = tableId;
    }

    /** @return the table associated with this PageId */
    public int getTableId() {
        // some code goes here
        return this.tableID;
    }

    /**
     * @return the page number in the table getTableId() associated with
     *   this PageId
     */
    public int getPageNumber() {
        // some code goes here
        return this.pageNumber;
    }

    /**
     * @return a hash code for this page, represented by the concatenation of
     *   the table number and the page number (needed if a PageId is used as a
     *   key in a hash table in the BufferPool, for example.)
     * @see BufferPool
     */
    public int hashCode() {
        // some code goes here
        //throw new UnsupportedOperationException("implement this");
    	String hashString = String.valueOf(this.getTableId()) + String.valueOf(this.pageNumber);
    	return hashString.hashCode();
//    	return Integer.parseInt(String.valueOf(this.getTableId()) + String.valueOf(this.getPageNumber()));
    }

    /**
     * Compares one PageId to another.
     *
     * @param o The object to compare against (must be a PageId)
     * @return true if the objects are equal (e.g., page numbers and table
     *   ids are the same)
     */
    public boolean equals(Object o) {
        // some code goes here
    	if(o instanceof HeapPageId) {
    		HeapPageId HPIDo = (HeapPageId) o;
    		if(this.getPageNumber() == HPIDo.getPageNumber() & this.getTableId() == HPIDo.getTableId()) {
    			return true;
    		}
    	}
        return false;
    }

    /**
     *  Return a representation of this object as an array of
     *  integers, for writing to disk.  Size of returned array must contain
     *  number of integers that corresponds to number of args to one of the
     *  constructors.
     */
    public int[] serialize() {
        int data[] = new int[2];

        data[0] = getTableId();
        data[1] = getPageNumber();

        return data;
    }

}
