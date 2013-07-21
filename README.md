# kindle-highlights

A Clojure library designed to download highlights from kindle.amazon.com (till amazon decides to give us an API to do this anyway).

## Usage

### Inside the REPL:

To save your reading metadata: (This will step through the pagination
on kindle.amazon.com/your_reading

<pre>
(navigate-login-fetch-book-links "your_email_id@email_id_provider" "your_password" "books-authors-permalink.txt")
</pre>

To save all quotes by Kurt Vonnegut:

<pre>
(fetch-highlights "books-authors-permalink.txt" (fn [s] (re-find #"Vonnegut" (nth s 2))) "your_email_id@email_id_provider" "your_password" "vonnegut-quotes.txt")
</pre>

### From the (slightly clunky) command-line:

To dump book metadata:

<pre>
lein run your_email_id@email_id_provider your_password books-authors-permalink2.txt --book-list
</pre>

To save all quotes by vonnegut:

<pre>
lein run your_email_id@email_id_provider your_password vonnegut-quotes-full.txt --highlights --book-details books-authors-permalink2.txt --author Vonnegut
</pre>


## License

Copyright © 2013 Shriphani Palakodety

Distributed under the Eclipse Public License, the same as Clojure.
