# Mini-design document

## What and how we'll be doing here

  * Step 1: open all epub files, and move all the resources to the new file. Each resource is assigned new id/href - we must maintain the link between old id/href and the new one (unclear by which one should we key this though).

  * Step 2: re-generate cover page based on new resource names

  * Step 3: re-generate TOC

  * Step 4: update all internal references (optional)

## We need plan B

Right, so here's the new plan. I know we won't be able to support absolutely everything this way but at very least we'll be able to compile something usable.

  * Step 1: Identify all text files, figure out in which order they go, use some heuristic to get the title of each file

## Plan C: we don't handle multiple TOC navpoints to a single physical resource

This happens when TOC contains references like `file1.xhtml#0000`. This means the whole process have to be very much TOC driven since this is the definitive source of truth.

The process will be as follows:

   * Go through each book, navigate through TOC
   * For each TOC entry
     * Check if we have this resource in the new book. If not:
        * Add new resource to the book, renaming it to include the index of the book across all epubs
        * Mark this resource as added in some internal map (if needed)
     *



      * Book Index
      * New resource name
      * Anchor name if any (`#xyz` thingie).
   * Together these three things will produce the HREF in the new book.