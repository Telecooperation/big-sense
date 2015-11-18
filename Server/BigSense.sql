-- phpMyAdmin SQL Dump
-- version 3.4.11.1deb2+deb7u2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Erstellungszeit: 09. Nov 2015 um 23:15
-- Server Version: 5.5.46
-- PHP-Version: 5.4.45-0+deb7u2

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Datenbank: `BigSense`
--

-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `AppPhoneStates`
--

CREATE TABLE IF NOT EXISTS `AppPhoneStates` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `AVID` int(11) NOT NULL,
  `imei` varchar(50) NOT NULL,
  `lastrestart` varchar(100) NOT NULL,
  `state` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `AppPhoneStates_ibfk_1` (`AVID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=138 ;


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `AppVersion`
--

CREATE TABLE IF NOT EXISTS `AppVersion` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `packagename` varchar(150) NOT NULL,
  `lastchange` varchar(50) NOT NULL,
  `filename` varchar(100) NOT NULL,
  `config` varchar(2000) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `AppVersion_Groups`
--

CREATE TABLE IF NOT EXISTS `AppVersion_Groups` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `AVID` int(11) NOT NULL,
  `GroupID` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `AppVersion_Groups_ibfk_1` (`AVID`),
  KEY `AppVersion_Groups_ibfk_2` (`GroupID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=147 ;


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `Groups`
--

CREATE TABLE IF NOT EXISTS `Groups` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=38 ;


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `Group_Phones`
--

CREATE TABLE IF NOT EXISTS `Group_Phones` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `GroupID` int(11) NOT NULL,
  `PhoneID` int(11) NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `GroupID` (`GroupID`),
  KEY `PhoneID` (`PhoneID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=327 ;


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `Log_Connections`
--

CREATE TABLE IF NOT EXISTS `Log_Connections` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `phoneID` int(11) NOT NULL,
  `timestamp` varchar(100) NOT NULL,
  `log` varchar(10000) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=24 ;


-- --------------------------------------------------------

--
-- Tabellenstruktur für Tabelle `Phones`
--

CREATE TABLE IF NOT EXISTS `Phones` (
  `ID` int(11) NOT NULL AUTO_INCREMENT,
  `imei` varchar(30) NOT NULL,
  `lastcontact` varchar(50) NOT NULL,
  `realname` varchar(100) NOT NULL,
  `batterylevel` int(11) NOT NULL,
  `batterytemperature` varchar(10) NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=8 ;


--
-- Constraints der Tabelle `AppPhoneStates`
--
ALTER TABLE `AppPhoneStates`
  ADD CONSTRAINT `AppPhoneStates_ibfk_1` FOREIGN KEY (`AVID`) REFERENCES `AppVersion` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints der Tabelle `AppVersion_Groups`
--
ALTER TABLE `AppVersion_Groups`
  ADD CONSTRAINT `AppVersion_Groups_ibfk_1` FOREIGN KEY (`AVID`) REFERENCES `AppVersion` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `AppVersion_Groups_ibfk_2` FOREIGN KEY (`GroupID`) REFERENCES `Groups` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints der Tabelle `Group_Phones`
--
ALTER TABLE `Group_Phones`
  ADD CONSTRAINT `Group_Phones_ibfk_1` FOREIGN KEY (`GroupID`) REFERENCES `Groups` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `Group_Phones_ibfk_2` FOREIGN KEY (`PhoneID`) REFERENCES `Phones` (`ID`) ON DELETE CASCADE ON UPDATE CASCADE;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
