Functional sql is an attempt to make a more functional version of sql. 

Functionalsql is written as a compiler (very WIP) which takes as input 'functional sql' and creates as output plain sql.

Functionalsql is less lengthy than sql because each function takes care of creating its own sql clauses.

Also information about the database (for instance how tables are relating to each other) can be fed to the compiler in advance. 
The FS compiler takes this information into account when creating the sql.

Is is also possible to extend the compiler and create a custom compiler with specific commands designed for a specific database.

Some examples:

Functional SQL -> SQL
a -> SELECT * FROM a
(a) -> SELECT * FROM a
a join( b, veld1, veld2) -> SELECT * FROM a t0, b t1 WHERE t0.veld1 = t1.veld2
a join( b ) sum(1, a.veld1, b.veld2) -> SELECT t0.veld1, t1.veld2 FROM a t0, b t1 WHERE a.id = b.id GROUP BY a.veld1, b.veld2
a join( (b join(c)), id, id ) -> SELECT * FROM a t0, (SELECT * FROM b t0, c t1 WHERE t0.id = t1.id) t1 WHERE t0.id = t1.id


NOTE: for the most part it is written in Java7. There are small Java8 'things'.