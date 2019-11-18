#Notes on resolved problems
* How to handle over sell a product?
    * Case 1: Repeated buying 
        * Solution: use productId + userPhoneNum (composite primary key) as pk of order table.
    * Case 2: stockCount in Redis is inconsistent to stockCount in Postgres
        * Solution: Instead of decrement stockCount in Postgres, I decrement it in Redis, and wrap the request of placing order as a message and send to MQ.
* How to handle hacks from different people(loopholes)?
    * Hashed the url: instead of putting an url like "/product/productId", using random salt to hash url and place it in Front End can prevent the url is tampered.
    * Limiting access from same IP