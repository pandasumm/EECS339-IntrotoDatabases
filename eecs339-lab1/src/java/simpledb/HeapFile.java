package simpledb;

import java.io.*;
import java.util.*;

import sun.net.ftp.FtpDirEntry.Permission;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 * 
 * @see simpledb.HeapPage#HeapPage
 * @author Sam Madden
 */
public class HeapFile implements DbFile {

    /**
     * Constructs a heap file backed by the specified file.
     * 
     * @param f
     *            the file that stores the on-disk backing store for this heap
     *            file.
     */
	private File file;
	private TupleDesc td;
    public HeapFile(File f, TupleDesc td) {
        // some code goes here
    	this.file = f;
    	this.td = td;
    }
    //private ArrayList<Page> pages;

    /**
     * Returns the File backing this HeapFile on disk.
     * 
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // some code goes here
        return this.file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     * 
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // some code goes here
    	return this.file.getAbsoluteFile().hashCode();
        // throw new UnsupportedOperationException("implement this");
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     * 
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // some code goes here
    	return this.td;
        //throw new UnsupportedOperationException("implement this");
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid)  throws IllegalArgumentException{
        // some code goes here
        try {
        	RandomAccessFile RAF = new RandomAccessFile(this.file, "r");
        	int offset = pid.getPageNumber()*BufferPool.getPageSize();
        	byte[] b = new byte[BufferPool.getPageSize()];
        	RAF.seek(offset);
        	RAF.read(b, 0, BufferPool.getPageSize());
        	RAF.close();
        	return new simpledb.HeapPage((HeapPageId)pid, b);
        }catch (IOException e) {
        	throw new IllegalArgumentException("page not exist");
        }
    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // some code goes here
        // not necessary for lab1
    	try {
            HeapPageId hpid= (HeapPageId)page.getId();
            RandomAccessFile rAf=new RandomAccessFile(this.file,"rw");
            int offset = hpid.getPageNumber()*BufferPool.getPageSize();
            byte[] b=new byte[BufferPool.getPageSize()];
            b=page.getPageData();
            rAf.seek(offset);
            rAf.write(b, 0, BufferPool.getPageSize());
            rAf.close();          
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // some code goes here
        return (int)Math.ceil(1.0 * this.getFile().length()/BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // some code goes here
        //return null;
        // not necessary for lab1
    	ArrayList<Page> pages = new ArrayList<Page>(); 	
    	int tableid=this.getId();
        // System.out.println("heapfile insert: numpages: " + this.numPages());
		for (int i=0; i<this.numPages();i++){
			
			HeapPageId pid= new HeapPageId(tableid,i);
			HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_WRITE);
			if (page.getNumEmptySlots() > 0){
				page.insertTuple(t);
			    pages.add(page);
			    return pages;
			}
	    }
		HeapPageId pid=new HeapPageId(this.getId(), this.numPages());
        HeapPage page=new HeapPage(pid, HeapPage.createEmptyPageData());
        page.insertTuple(t);
        pages.add(page);
        this.writePage(page);
        return pages;
    	
    }

    // see DbFile.java for javadocs
    public ArrayList<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // some code goes here
        // return null;
        // not necessary for lab1
    	ArrayList<Page> pages = new ArrayList<Page>();

		PageId pid= t.getRecordId().getPageId();
		HeapPage page = (HeapPage) Database.getBufferPool().getPage(tid, pid, Permissions.READ_WRITE);
		page.deleteTuple(t);
	    pages.add(page);
	    return pages;
    }

    public class heapFileIterator implements DbFileIterator{
    	private TransactionId tid;
    	private HeapFile heapfile;
    	private int pageNumber;
    	private Iterator<Tuple> tupleIterator;
    	
    	public heapFileIterator(TransactionId tid, HeapFile file) {
    		this.heapfile = file;
    		this.tid = tid;
    	}
    	public void open() throws DbException, TransactionAbortedException{
    		//System.out.println("open the it: "+this.heapfile.numPages());
    		this.pageNumber = 0;
    		PageId pageId = new HeapPageId(this.heapfile.getId(), this.pageNumber);
    		HeapPage heapPage = (HeapPage) Database.getBufferPool().getPage(this.tid, pageId, Permissions.READ_ONLY);
    		this.tupleIterator = heapPage.iterator();
    	}
    	
    	public Tuple next() throws DbException, TransactionAbortedException, NoSuchElementException{
    		if(this.tupleIterator == null) {
    			throw new NoSuchElementException("no tuple");
    		}
    		if(tupleIterator.hasNext()) {
    			return tupleIterator.next();
    		}
    		else {
    			
    			if(this.pageNumber < this.heapfile.numPages()-1) {
    				this.pageNumber += 1;
    				PageId pageId = new HeapPageId(this.heapfile.getId(), this.pageNumber);
    				HeapPage heappage = (HeapPage) Database.getBufferPool().getPage(this.tid, pageId, Permissions.READ_ONLY);
    				//System.out.println(heappage.tuples.length);
    				this.tupleIterator = heappage.iterator();
    				if(this.tupleIterator.hasNext()) {
    					return this.tupleIterator.next();
    				}
    			}
    		}
    		throw new NoSuchElementException();
    	}
    	
    	public boolean hasNext() throws DbException, TransactionAbortedException{
    		//System.out.println("has next: " + this.pageNumber);
    		if(this.tupleIterator != null) {
    			if(this.tupleIterator.hasNext()) {
        			return true;
        		}
        		else {
        			if(this.pageNumber < this.heapfile.numPages()-1) {
        				//System.out.println("has next: " + (this.pageNumber+1));
        				PageId pageId = new HeapPageId(this.heapfile.getId(), this.pageNumber+1);
        				HeapPage heappage = (HeapPage)Database.getBufferPool().getPage(this.tid, pageId, Permissions.READ_ONLY);
        				return heappage.iterator().hasNext();
        			}
        		}
    		}
    		return false;
    	}
    	
    	public void rewind() throws DbException, TransactionAbortedException{
    		close();
    		open();
    	}
    	public void close() {
    		this.tupleIterator = null;
    	}
    	
    }
    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // some code goes here
    	
        return new heapFileIterator(tid, this);
    }
    

}

