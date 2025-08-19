insert into products (name, price, stock, category, is_active, created_at) values ('Laptop Pro', 1200.00, 50, 'Electronics', true, NOW());
insert into products (name, price, stock, category, is_active, created_at) values ('Smartphone X', 800.00, 150, 'Electronics', true, NOW());
insert into products (name, price, stock, category, is_active, created_at) values ('Office Chair', 150.50, 200, 'Furniture', true, NOW());
insert into products (name, price, stock, category, is_active, created_at) values ('Coffee Mug', 25.00, 500, 'Kitchenware', true, NOW());
insert into products (name, price, stock, category, is_active, created_at) values ('Java Programming Book', 49.99, 100, 'Books', true, NOW());

insert into orders (customer_name, customer_email, order_date, status, total_amount) values ('John Doe', 'john.doe@example.com', NOW(), 'PENDING', 2550.50);

insert into order_items (order_id, product_id, quantity, unit_price, total_price) values (1, 1, 2, 1200.00, 2400.00);
insert into order_items (order_id, product_id, quantity, unit_price, total_price) values (1, 3, 1, 150.50, 150.50);