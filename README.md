Do you also dislike big and complex sql statements?
Or lose count over all the aliases you created in a SQL statement?
Or are you completely annoyed to type over and over the same type
of sql for a certain database?

Don't worry anymore, there is now Functional SQL (FS). FS divides the sql language
up into very small and simple functions, which you can combine to create very complex SQL statements. 
You do not have to worry about aliases anymore.

Also it is possible to write you own specific functions for 
a specific database. In this way you can avoid to write again the same SQL for a certain database. Just use
the specific functions you created. 

And if you dislike the name of certain standard FS function? Just give it a new name.

This project consists of a compiler which takes as input an FS-statement and outputs the relating SQL-statement. The compiler can be fed
in advance information of your database. For instance how tables are relating to each other. This information
is used when creating the SQL queries.

An example:

The FS statement 

    a fulljoin(b, leftjoin(c, id, id), id, id) 
    
will create the following SQL statement:

    SELECT * FROM a t0 FULL JOIN b t1 ON t0.id = t1.id LEFT JOIN c t2 ON t0.id = t2.id

Notice that the tables are joined on the id fields. This information can be fed in advance to the FS compiler. If so,
the following FS-statement will be sufficient:

    a fulljoin(b, leftjoin(c))

Some more examples (Functional SQL -> SQL):

    a -> SELECT * FROM a

    (a) -> SELECT * FROM a

    a join( b, veld1, veld2) -> SELECT * FROM a t0, b t1 WHERE t0.veld1 = t1.veld2

    a join( b ) sum(1, a.veld1, b.veld2) -> SELECT t0.veld1, t1.veld2 FROM a t0, b t1 WHERE a.id = b.id GROUP BY a.veld1, b.veld2

    a join( (b join(c)), id, id ) -> SELECT * FROM a t0, (SELECT * FROM b t0, c t1 WHERE t0.id = t1.id) t1 WHERE t0.id = t1.id

    ((((a))) filter(field, 2)) -> SELECT * FROM a t0 WHERE field = 2
    
    a join(b) group(field, b.field) -> SELECT field, t1.field FROM a t0, b t1 WHERE t0.id = t1.id GROUP BY field, t1.field