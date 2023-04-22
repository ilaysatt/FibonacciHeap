/**
 * FibonacciHeap
 *
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap
{
	public HeapNode min;
	public int size;
	public HeapNode sentinel = new HeapNode(true);
	public static int links;
	public int marked_nodes;
	public static int cuts;
	public int num_of_trees = 0;

   /**
    * public boolean isEmpty()
    *
    * Returns true if and only if the heap is empty.
    *   
    */
    public boolean isEmpty()
    {
    	return min == null;
    }
		
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
    * The added key is assumed not to already belong to the heap.  
    * 
    * Returns the newly created node.
    */
    public HeapNode insert(int key)
    {
    	HeapNode new_node = new HeapNode(key);
    	if(this.isEmpty()) {
    		sentinel.next = new_node;
    		sentinel.prev = new_node;
    		new_node.prev = sentinel;
    		new_node.next = sentinel;
    		min = new_node;
    	} else {
    		new_node.next = sentinel.next;
    		sentinel.next.prev = new_node;
    		new_node.prev = sentinel;
    		sentinel.next = new_node;
    		if(new_node.key < min.key) {
    			min = new_node;
    		}
    	}
    	size += 1;
    	num_of_trees += 1;
    	return new_node;
    }

   /**
    * public void deleteMin()
    *
    * Deletes the node containing the minimum key.
    *
    */
    public void deleteMin()
    {
    	HeapNode min_node = min;
    	// If min is null then heap is empty and there is no node to delete
    	if(min_node != null) {
    		// Insert min's children into root list and remove min
    		if(min_node.child != null) {
    			HeapNode child = min_node.child;
    			child.prev = min_node.prev;
    			min_node.prev.next = child;
    			while(! child.next.isSentinel()) {
    				if(child.mark) { // unmark node and subtract from counter
    					child.mark = false;
    					marked_nodes -= 1;
    				}
    				child.parent = null;
    				num_of_trees += 1;
    				child = child.next;
    			}
    			child.next = min_node.next;
    			min_node.next.prev = child;
    			child.parent = null;
    			if(child.mark) {
	    			child.mark = false;
	    			marked_nodes -= 1;
    			}
    			
    		} else {
    			min_node.prev.next = min_node.next;
    			min_node.next.prev = min_node.prev;
    		}
    		num_of_trees -= 1;
    		
    		// Choose arbitrary minimum that will be changed once heap is consolidated
    		min = sentinel.next;
    		
    		// Consolidate heap
    		this.consolidate();
    		
    		
    		// Decrease size by one
    		size -= 1;

    	}
     	return; // should be replaced by student code
     	
    }
    
    public void consolidate() {
    	// Create a new array that's the size of the heap (potential maximum rank of node)
    	HeapNode[] arr = new HeapNode[log2(size) + 2];
    	
    	
    	// Iterate over root list of heap
    	HeapNode iter_node = sentinel.next;
    	while(! iter_node.isSentinel()) {
    		HeapNode node1 = iter_node;
    		iter_node = iter_node.next; // for next iteration
    		int d = node1.degree;
    		
    		while(arr[d] != null) {
    			HeapNode node2 = arr[d];
    			if(node1.key > node2.key) { // Switch node1 and node2, needed for linking
    				HeapNode tmp = node1;
    				node1 = node2;
    				node2 = tmp;
    			
    			}
    			heapLink(node1, node2);
    			arr[d] = null;
    			d += 1;
    		}
    		arr[d] = node1;
    	}
    	
    	// Insert linked heaps from array to heap
    	int real_size = size;
    	sentinel.next = null;
    	sentinel.prev = null;
    	min = null;
    	num_of_trees = 0;
    	for(int i = log2(real_size) + 2 - 1; i >= 0; i--) {
    		if(arr[i] != null) {
    			if(sentinel.next == null) {
    				sentinel.next = arr[i];
    				sentinel.prev = arr[i];
    				arr[i].prev = sentinel;
    				arr[i].next = sentinel;
    			} else {
	    			arr[i].prev = sentinel;
	    			arr[i].next = sentinel.next;
	    			sentinel.next.prev = arr[i];
	    			sentinel.next = arr[i];//up to here
    			}
    			if(min == null) {
    				min = arr[i];
    			} else if(min.key > arr[i].key) {
    				min = arr[i];
    			}
    			num_of_trees += 1;
    		}
    	}
    	size = real_size;
    	return;
    }
    
    public void heapLink(HeapNode node1, HeapNode node2) {
    	// Removing node2 from root list
    	node2.prev.next = node2.next;
    	node2.next.prev = node2.prev;
    	num_of_trees -= 1;
    	
    	// Making node2 a child of node1
    	if(node1.child == null) {
    		// Create new sentinel for node1 children list
    		HeapNode sent = new HeapNode(true);
    		node2.prev = sent; node2.next = sent;
    		sent.next = node2; sent.prev = node2;
    	}
    	else {
    		HeapNode sent = node1.child.prev; // Existing sentinel in node1 children list
    		node2.next = sent.next;
    		sent.next.prev = node2;
    		node2.prev = sent;
    		sent.next = node2;
    	}
		node1.child = node2;
		node2.parent = node1;
		
		// Increasing degree of node1 by one
		node1.degree += 1;
		
		// Re-marking node2
		if(node2.mark) {
			node2.mark = false;
			marked_nodes -= 1;
		}
		
		// Increasing links by one
		links += 1;
    }

   /**
    * public HeapNode findMin()
    *
    * Returns the node of the heap whose key is minimal, or null if the heap is empty.
    *
    */
    public HeapNode findMin()
    {
    	return min;
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Melds heap2 with the current heap.
    *
    */
    public void meld (FibonacciHeap heap2)
    {
    	// Edge cases
    	if(heap2.isEmpty()) {
    		return;
    	}
    	else if (this.isEmpty()) {
    		this.min = heap2.min;
    		this.sentinel = heap2.sentinel;
    		this.size = heap2.size;
    		this.links = heap2.links;
    		this.marked_nodes = heap2.marked_nodes;
    		this.cuts = heap2.cuts;
    		this.num_of_trees = heap2.num_of_trees;
    		return;
    	}
    	
    	// Concatenate the two root lists
    	HeapNode last = heap2.sentinel.prev;
    	HeapNode first_heap2 = heap2.sentinel.next;
    	HeapNode last_heap1 = this.sentinel.prev;
    	last.next = this.sentinel;
    	this.sentinel.prev = last;
    	last_heap1.next = first_heap2;
    	first_heap2.prev = last_heap1;
    	
    	// Update minimum if needed
    	if(heap2.min.key < this.min.key) {
    		this.min = heap2.min;
    	}
    	
    	// Sum other instances
    	this.size = this.size + heap2.size;
    	this.links = this.links + heap2.links;
    	this.marked_nodes = this.marked_nodes + heap2.marked_nodes;
    	this.cuts = this.cuts + heap2.cuts;
    	this.num_of_trees = this.num_of_trees + heap2.num_of_trees;
    	
    }

   /**
    * public int size()
    *
    * Returns the number of elements in the heap.
    *   
    */
    public int size()
    {
    	return size;
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
    * (Note: The size of of the array depends on the maximum order of a tree.)  
    * 
    */
    public int[] countersRep()
    {
    	if(!isEmpty()) {
	    	//int[] arr = new int[log2(size) + 2];
    		int[] arr = new int[size];
	    	HeapNode iter = sentinel.next;
	    	while(!iter.isSentinel()) {
	    		arr[iter.degree] += 1;
	    		iter = iter.next;
	    	}
	    	if(arr[arr.length - 1] == 0) {
	    		int new_array_size = 0;
	    		for(int i = 0; i < arr.length; i++) {
	    			if(arr[i] != 0) {
	    				new_array_size = i + 1;
	    			}
	    		}
	    		int[] new_arr = new int[new_array_size];
	    		for(int i = 0; i < new_arr.length; i++) {
	    			new_arr[i] = arr[i];
	    		}
	    		return new_arr;
	    	}
	        return arr; //	 to be replaced by student code
    	}
    	int[] arr = new int[]{};
    	return arr;
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap.
	* It is assumed that x indeed belongs to the heap.
    *
    */
    public void delete(HeapNode x) 
    {

    	decreaseKey(x, x.key - min.key + 1);
    	deleteMin();
    	
	   
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
    * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
    */
    public void decreaseKey(HeapNode x, int delta)
    {
    	// Change x's key to new value
    	x.key = x.key - delta;
    	HeapNode y = x.parent;
    	// Perform cuts if necessary
    	if(y != null && x.key < y.key) {
    		cut(x, y);
    		cascadingCut(y);
    	}
    	// Reconfigure min if necessary
    	if(x.key < min.key) {
    		min = x;
    	}
    }
    
    public void cut(HeapNode x, HeapNode y) {
    	// Removing x from child list of y
    	x.parent = null;
    	if(x.mark) {
    		x.mark = false;
    		marked_nodes -= 1;
    	}
    	y.degree -= 1;
    	if(x.next.isSentinel() && x.prev.isSentinel()) {
    		y.child = null;
    	}
    	else {
    		if(y.child == x) {
    			HeapNode new_child = x.next;
    			y.child = new_child;
    			new_child.parent = y;
    			new_child.prev = x.prev;
    			x.prev.next = new_child;
    		} else {
    			x.prev.next = x.next;
    			x.next.prev = x.prev;
    		}
    	}
    	// Putting x in root list
    	HeapNode tmp = sentinel.next;
    	sentinel.next = x;
    	x.prev = sentinel;
    	x.next = tmp;
    	tmp.prev = x;
    	
    	num_of_trees += 1;
    	cuts += 1;
    }
    public void cascadingCut(HeapNode y) {
    	HeapNode z = y.parent;
    	// No need to check root nodes
    	if(z != null) {
    		// If y isn't marked, mark it and end recursion
    		if (! y.mark) {
    			y.mark = true;
    			marked_nodes += 1;
    		}
    		// y was marked, cut it and continue recursion to parents
    		else {
    			cut(y, z);
    			cascadingCut(z);
    		}
    	}
    }

   /**
    * public int nonMarked() 
    *
    * This function returns the current number of non-marked items in the heap
    */
    public int nonMarked() 
    {    
        return size - marked_nodes; // should be replaced by student code
    }

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * 
    * In words: The potential equals to the number of trees in the heap
    * plus twice the number of marked nodes in the heap. 
    */
    public int potential() 
    {    
        return num_of_trees + 2 * marked_nodes; // should be replaced by student code
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the
    * run-time of the program. A link operation is the operation which gets as input two
    * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
    * tree which has larger value in its root under the other tree.
    */
    public static int totalLinks()
    {
    	return links;
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the
    * run-time of the program. A cut operation is the operation which disconnects a subtree
    * from its parent (during decreaseKey/delete methods). 
    */
    public static int totalCuts()
    {    
    	return cuts;
    }

     /**
    * public static int[] kMin(FibonacciHeap H, int k) 
    *
    * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
    * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
    *  
    * ###CRITICAL### : you are NOT allowed to change H. 
    */
    public static int[] kMin(FibonacciHeap H, int k)
    {
        int[] arr = new int[k];
        // Edge case
        if(H.isEmpty()) {
        	return arr;
        }
        
        // Creating helper heap that will include minimums and their children
        FibonacciHeap helper = new FibonacciHeap();
        HeapNode node = H.min;
        helper.insert(node.key);
        for(int i = 0; i < k; i++) {
        	// Insert node's children into helper heap
        	if(node.child != null) {
        		node = node.child;
	        	while(!node.isSentinel()) {
	        		helper.insert(node.key);
	        		helper.sentinel.next.kMin_helper = node; // Pointers that will help find the next minimum in the original heap
	        		node = node.next;
	        	}
	        	node = node.next;
        	}
        	
        	// Inserting minimum of helper heap into array and deleting minimum in helper
        	arr[i] = helper.findMin().key;
        	helper.deleteMin();
        	if(!helper.isEmpty()) {
        		node = helper.findMin().kMin_helper;  // Jump to next minimum in original heap
        	}
        }
        return arr;
    }
 
    



    
   /**
    * public class HeapNode
    * 
    * If you wish to implement classes other than FibonacciHeap
    * (for example HeapNode), do it in this file, not in another file. 
    *  
    */
    public static class HeapNode{

    	public int key;
    	public int degree;
    	public boolean mark;
    	public HeapNode child;
    	public HeapNode next;
    	public HeapNode prev;
    	public HeapNode parent;
    	
    	public boolean sentinel = false;
    	
    	public HeapNode kMin_helper;  // Used only in kMin

    	public HeapNode(int key) {
    		this.key = key;
    		this.mark = false;
    	}
    	
    	public HeapNode(boolean sentinel) {
    		this.sentinel = true;
    	}
    	
    	public HeapNode(int key, HeapNode kMin_helper) {
    		this.key = key;
    		this.kMin_helper = kMin_helper;
    	}

    	public int getKey() {
    		return this.key;
    	}
    	
    	public boolean isSentinel() {
    		return this.sentinel;
    	}

    	
    	
    }
    
    
    public static int log2(int i) {
		return (int) (Math.log(i) / Math.log(2));
	}
}
