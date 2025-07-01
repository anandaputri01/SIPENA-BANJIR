-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Jul 01, 2025 at 03:01 PM
-- Server version: 8.0.30
-- PHP Version: 8.4.6

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `flood_reporting_db`
--

-- --------------------------------------------------------

--
-- Table structure for table `messages`
--

CREATE TABLE `messages` (
  `id` int NOT NULL,
  `report_id` int NOT NULL,
  `sender_id` int NOT NULL,
  `message` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_admin_message` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `messages`
--

INSERT INTO `messages` (`id`, `report_id`, `sender_id`, `message`, `is_admin_message`, `created_at`) VALUES
(9, 19, 3, 'Banjir telah teratasi', 1, '2025-06-25 03:55:43'),
(13, 23, 3, 'Akan segera kami tindaklanjuti', 1, '2025-07-01 07:59:27'),
(14, 23, 3, 'Banjir sudah surut', 1, '2025-07-01 08:01:17');

-- --------------------------------------------------------

--
-- Table structure for table `reports`
--

CREATE TABLE `reports` (
  `id` int NOT NULL,
  `user_id` int NOT NULL,
  `reporter_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `reporter_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `flood_location` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `latitude` decimal(10,8) DEFAULT NULL,
  `longitude` decimal(11,8) DEFAULT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `photo_path` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status` enum('PENDING','REVIEWED','RESOLVED') COLLATE utf8mb4_unicode_ci DEFAULT 'PENDING',
  `admin_notes` text COLLATE utf8mb4_unicode_ci,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `reports`
--

INSERT INTO `reports` (`id`, `user_id`, `reporter_name`, `reporter_phone`, `flood_location`, `latitude`, `longitude`, `description`, `photo_path`, `status`, `admin_notes`, `created_at`, `updated_at`) VALUES
(19, 6, 'Ananda', '088232653256', 'Terboyo Kulon, Jalan Hassanudin Blok B', -6.95062424, 110.44830322, 'Banjir sudah 2 hari dan mencapai ketinggian 2 meterr', 'uploads\\download.jpg', 'RESOLVED', 'Banjir telah teratasi', '2025-06-25 03:52:31', '2025-06-25 03:55:40'),
(23, 8, 'putri', '088265432176', 'Karang Kidul, Jalan Manggis', -6.96527840, 110.40641785, 'Banjir datang dari pagi dan sudah mencapai ketinngian 2,5 meter', 'uploads\\download.jpg', 'RESOLVED', 'Banjir sudah surut', '2025-07-01 07:56:08', '2025-07-01 08:01:14');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int NOT NULL,
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `is_admin` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `password`, `email`, `phone`, `is_admin`, `created_at`) VALUES
(1, 'adit', 'password', 'wp@gmail.com', '5478980989', 0, '2025-06-15 16:14:29'),
(2, 'wp', 'password', 'sd@gmai.com', '16918919', 0, '2025-06-15 16:55:45'),
(3, 'admin', 'password', 'admin@gmail.com', '081234567890', 1, '2025-06-15 20:37:59'),
(5, 'wps', 'password', 'wps@gmail.com', '669707', 0, '2025-06-16 04:18:57'),
(6, 'Ananda', 'ananda123', 'ananda@gmail.com', '088232653256', 0, '2025-06-18 11:01:42'),
(7, 'yohana', 'yohana123', 'yohana@gmail.com', '082133678015', 0, '2025-07-01 07:03:01'),
(8, 'putri', 'putri123', 'putri@gmail.com', '088265432176', 0, '2025-07-01 07:53:56');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `messages`
--
ALTER TABLE `messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `report_id` (`report_id`),
  ADD KEY `sender_id` (`sender_id`);

--
-- Indexes for table `reports`
--
ALTER TABLE `reports`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user_id` (`user_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `messages`
--
ALTER TABLE `messages`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `reports`
--
ALTER TABLE `reports`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=24;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `messages`
--
ALTER TABLE `messages`
  ADD CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`report_id`) REFERENCES `reports` (`id`),
  ADD CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`);

--
-- Constraints for table `reports`
--
ALTER TABLE `reports`
  ADD CONSTRAINT `reports_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
