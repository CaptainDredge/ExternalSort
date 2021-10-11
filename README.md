## ExternalSort

Very crude but minimal implementation of external sort algorithm

### Working of Algorithm

1. Map: Break large file into smaller chunks and sort those chunks
2. Reduce: If `k` chunks are generated, do a `k` way merge of sorted list using a priority queue and keep storing the minimum element of priority queue into a large file which will be the sorted version of our original file once the reduction completes