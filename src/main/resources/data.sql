-- Sample car data for CarMatchAI
-- This data will be loaded at application startup

INSERT INTO cars 
(brand, model, car_year, price, horse_power, fuel_consumption, fuel_type, is_compact, drivetrain_type, color) VALUES
-- Compact cars for city driving
('Toyota', 'Corolla', 2023, 25000.00, 139, 5.8, 'Gasoline', true, 'FWD', 'White'),
('Honda', 'Civic', 2023, 26000.00, 158, 6.2, 'Gasoline', true, 'FWD', 'Silver'),
('Nissan', 'Sentra', 2023, 22000.00, 149, 6.5, 'Gasoline', true, 'FWD', 'Black'),
('Hyundai', 'Elantra', 2023, 23000.00, 147, 6.0, 'Gasoline', true, 'FWD', 'Blue'),
('Kia', 'Forte', 2023, 21000.00, 147, 6.3, 'Gasoline', true, 'FWD', 'Red'),

-- Mid-size sedans
('Toyota', 'Camry', 2023, 32000.00, 203, 7.2, 'Gasoline', false, 'FWD', 'White'),
('Honda', 'Accord', 2023, 33000.00, 192, 7.0, 'Gasoline', false, 'FWD', 'Silver'),
('Nissan', 'Altima', 2023, 30000.00, 188, 7.5, 'Gasoline', false, 'FWD', 'Black'),
('Hyundai', 'Sonata', 2023, 31000.00, 191, 7.3, 'Gasoline', false, 'FWD', 'Blue'),
('Kia', 'K5', 2023, 29000.00, 180, 7.4, 'Gasoline', false, 'FWD', 'Red'),

-- Luxury sedans
('BMW', '3 Series', 2023, 45000.00, 255, 8.5, 'Gasoline', false, 'RWD', 'White'),
('Mercedes-Benz', 'C-Class', 2023, 47000.00, 255, 8.8, 'Gasoline', false, 'RWD', 'Silver'),
('Audi', 'A4', 2023, 43000.00, 248, 8.2, 'Gasoline', false, 'AWD', 'Black'),
('Lexus', 'IS', 2023, 42000.00, 241, 8.0, 'Gasoline', false, 'RWD', 'Blue'),
('Infiniti', 'Q50', 2023, 41000.00, 300, 9.2, 'Gasoline', false, 'RWD', 'Red'),

-- SUVs
('Toyota', 'RAV4', 2023, 35000.00, 203, 8.5, 'Gasoline', false, 'AWD', 'White'),
('Honda', 'CR-V', 2023, 36000.00, 190, 8.8, 'Gasoline', false, 'AWD', 'Silver'),
('Nissan', 'Rogue', 2023, 32000.00, 181, 9.0, 'Gasoline', false, 'AWD', 'Black'),
('Hyundai', 'Tucson', 2023, 33000.00, 187, 8.7, 'Gasoline', false, 'AWD', 'Blue'),
('Kia', 'Sportage', 2023, 31000.00, 187, 8.9, 'Gasoline', false, 'AWD', 'Red'),

-- Performance cars
('BMW', 'M3', 2023, 75000.00, 473, 12.5, 'Gasoline', false, 'RWD', 'White'),
('Mercedes-Benz', 'AMG C63', 2023, 78000.00, 469, 12.8, 'Gasoline', false, 'RWD', 'Silver'),
('Audi', 'RS4', 2023, 72000.00, 450, 12.2, 'Gasoline', false, 'AWD', 'Black'),
('Porsche', '911', 2023, 110000.00, 379, 11.8, 'Gasoline', false, 'RWD', 'Blue'),
('Chevrolet', 'Corvette', 2023, 65000.00, 495, 13.2, 'Gasoline', false, 'RWD', 'Red'),

-- Electric vehicles
('Tesla', 'Model 3', 2023, 45000.00, 283, 0.0, 'Electric', true, 'RWD', 'White'),
('Tesla', 'Model Y', 2023, 55000.00, 384, 0.0, 'Electric', false, 'AWD', 'Silver'),
('BMW', 'i4', 2023, 58000.00, 335, 0.0, 'Electric', false, 'RWD', 'Black'),
('Mercedes-Benz', 'EQS', 2023, 110000.00, 329, 0.0, 'Electric', false, 'RWD', 'Blue'),
('Audi', 'e-tron', 2023, 70000.00, 355, 0.0, 'Electric', false, 'AWD', 'Red'),

-- Budget-friendly options
('Nissan', 'Versa', 2023, 18000.00, 122, 5.5, 'Gasoline', true, 'FWD', 'White'),
('Hyundai', 'Accent', 2023, 19000.00, 120, 5.7, 'Gasoline', true, 'FWD', 'Silver'),
('Kia', 'Rio', 2023, 17000.00, 120, 5.6, 'Gasoline', true, 'FWD', 'Black'),
('Mitsubishi', 'Mirage', 2023, 16000.00, 78, 4.8, 'Gasoline', true, 'FWD', 'Blue'),
('Chevrolet', 'Spark', 2023, 15000.00, 98, 5.2, 'Gasoline', true, 'FWD', 'Red'),

-- Additional model years for broader range

('Toyota', 'Corolla', 2019, 19000.00, 132, 6.7, 'Gasoline', true, 'FWD', 'White'),
('Toyota', 'Corolla', 2021, 22000.00, 139, 6.1, 'Gasoline', true, 'FWD', 'Blue'),
('Toyota', 'Camry', 2018, 24000.00, 203, 7.8, 'Gasoline', false, 'FWD', 'Silver'),
('Toyota', 'RAV4', 2024, 36500.00, 203, 8.3, 'Gasoline', false, 'AWD', 'Gray'),
('Honda', 'Civic', 2020, 21000.00, 158, 6.4, 'Gasoline', true, 'FWD', 'Black'),
('Honda', 'Accord', 2019, 25000.00, 192, 7.4, 'Gasoline', false, 'FWD', 'White'),
('Honda', 'CR-V', 2024, 37000.00, 190, 8.6, 'Gasoline', false, 'AWD', 'Blue'),
('Nissan', 'Sentra', 2018, 16000.00, 124, 6.9, 'Gasoline', true, 'FWD', 'Red'),
('Nissan', 'Altima', 2021, 26000.00, 188, 7.6, 'Gasoline', false, 'FWD', 'Gray'),
('Nissan', 'Rogue', 2022, 28500.00, 181, 8.9, 'Gasoline', false, 'AWD', 'White'),
('Hyundai', 'Elantra', 2020, 18500.00, 147, 6.3, 'Gasoline', true, 'FWD', 'Silver'),
('Hyundai', 'Sonata', 2019, 22000.00, 185, 7.6, 'Gasoline', false, 'FWD', 'Blue'),
('Hyundai', 'Tucson', 2021, 27000.00, 187, 8.8, 'Gasoline', false, 'AWD', 'Black'),
('Kia', 'Forte', 2019, 17000.00, 147, 6.5, 'Gasoline', true, 'FWD', 'White'),
('Kia', 'K5', 2021, 26000.00, 180, 7.5, 'Gasoline', false, 'FWD', 'Gray'),
('Kia', 'Sportage', 2022, 28500.00, 187, 9.1, 'Gasoline', false, 'AWD', 'Blue'),
('BMW', '3 Series', 2018, 33000.00, 248, 8.9, 'Gasoline', false, 'RWD', 'Black'),
('BMW', '3 Series', 2024, 47000.00, 255, 8.3, 'Gasoline', false, 'RWD', 'White'),
('Mercedes-Benz', 'C-Class', 2019, 38000.00, 255, 9.0, 'Gasoline', false, 'RWD', 'Silver'),
('Audi', 'A4', 2020, 36000.00, 248, 8.6, 'Gasoline', false, 'AWD', 'Gray'),
('Lexus', 'IS', 2018, 32000.00, 241, 8.7, 'Gasoline', false, 'RWD', 'Blue'),
('Tesla', 'Model 3', 2020, 39000.00, 283, 0.0, 'Electric', true, 'RWD', 'White'),
('Tesla', 'Model 3', 2024, 48000.00, 283, 0.0, 'Electric', true, 'RWD', 'Red'),
('Tesla', 'Model Y', 2021, 52000.00, 384, 0.0, 'Electric', false, 'AWD', 'Black'),
('BMW', 'i4', 2022, 54000.00, 335, 0.0, 'Electric', false, 'RWD', 'Blue'),
('Audi', 'e-tron', 2020, 62000.00, 355, 0.0, 'Electric', false, 'AWD', 'White');