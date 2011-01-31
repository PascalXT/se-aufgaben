WITH 

ZweitStimmenNachBundesland AS (
	SELECT w2.ParteiID, wk.BundeslandID, sum(w2.Anzahl) as
		AnzahlStimmen
	FROM ZweitStimmenNachWahlkreis w2, Wahlkreis wk
	WHERE w2.WahlkreisID = wk.ID 
		AND w2.Jahr = 2009
	GROUP BY wk.BundeslandID, w2.ParteiID), 

ZweitStimmenNachPartei AS (
	SELECT ParteiID, SUM(AnzahlStimmen) AS AnzahlStimmen
	FROM ZweitStimmenNachBundesland
	GROUP BY ParteiID), 

MaxErststimmenNachWahlkreis AS (
	SELECT v.WahlkreisID, MAX(v.Anzahl) AS MaxStimmen
	FROM ErstStimmenNachWahlkreis v
	WHERE v.Jahr = 2009
	GROUP BY v.WahlkreisID), 

DirektmandateNummer AS (
	SELECT e.KandidatID, e.WahlkreisID, Row_Number() OVER
		(PARTITION BY e.WahlkreisID) AS Nummer
	FROM MaxErststimmenNachWahlkreis m,
		ErstStimmenNachWahlkreis e
	WHERE e.WahlkreisID = m.WahlkreisID 
		AND	m.MaxStimmen = e.Anzahl 
		AND e.Jahr = 2009), 

DirektmandateMaxNummer AS (
	SELECT WahlkreisID, MAX(Nummer) AS kMaxNummer
	FROM DirektmandateNummer
	GROUP BY WahlkreisID), 

Direktmandate AS (
	SELECT n.KandidatID, k.ParteiID, k.DMWahlkreisID
	FROM DirektmandateNummer n, DirektmandateMaxNummer mn,
		ZufallsZahlenDirektmandate z, Kandidat k
	WHERE n.WahlkreisID = mn.WahlkreisID 
		AND k.ID = n.KandidatID 
		AND z.Zeile = mod(n.WahlkreisID, ( SELECT COUNT(*) FROM
			ZufallsZahlenDirektmandate)) 
		AND n.Nummer = mod(z.Zahl, mn.kMaxNummer) + 1), 

FuenfProzentParteien AS (
	SELECT z.ParteiID
	FROM ZweitStimmenNachPartei z
	GROUP BY z.ParteiID
	HAVING CAST(SUM(z.AnzahlStimmen) AS FLOAT) / ( SELECT
		SUM(AnzahlStimmen) FROM ZweitStimmenNachPartei) >= 0.05), 

DreiDirektMandatParteien AS (
	SELECT dm.ParteiID
	FROM Direktmandate dm 
	GROUP BY dm.ParteiID
	HAVING COUNT(*) >= 3), 

ParteienImBundestag AS (
	SELECT *
	FROM FuenfProzentParteien

	UNION

	SELECT *
	FROM DreiDirektMandatParteien), 

Divisoren AS (
	SELECT (ROW_NUMBER() OVER (order by w.ID) - 0.5) as Wert
	FROM Wahlkreis w

	UNION

	SELECT (ROW_NUMBER() OVER (order by w.ID) + (
	SELECT COUNT(*)
		FROM Wahlkreis) - 0.5) AS Wert
	FROM Wahlkreis w), 

ZugriffsreihenfolgeSitzeNachPartei AS (
	SELECT p.ParteiID, z.AnzahlStimmen, (z.AnzahlStimmen /
		d.wert) as DivWert, ROW_NUMBER() OVER (ORDER BY
		(z.AnzahlStimmen / d.wert) DESC, rnd.Zahl DESC) as Rang
	FROM ParteienImBundestag p, ZweitStimmenNachPartei z,
		Divisoren d, ZufallsZahlenSitzeNachPartei rnd
	WHERE p.ParteiID = z.ParteiID 
		AND rnd.Zeile = MOD(p.ParteiID + (d.wert / 1)*( SELECT
			COUNT(*) FROM Partei), ( SELECT COUNT(*) FROM ZufallsZahlenSitzeNachPartei))), 

SitzeNachPartei AS (
	SELECT ParteiID, COUNT(Rang) as AnzahlSitze
	FROM ZugriffsreihenfolgeSitzeNachPartei
	WHERE Rang <= 2*( SELECT COUNT(*) FROM Wahlkreis)
	GROUP BY ParteiID), 

ZugriffsreihenfolgeSitzeNachLandeslisten AS (
	SELECT p.ParteiID, z.BundeslandID, z.AnzahlStimmen,
		(z.AnzahlStimmen / d.wert) as DivWert, ROW_NUMBER() OVER
		(PARTITION BY p.ParteiID ORDER BY (z.AnzahlStimmen / d.wert)
		DESC, rnd.Zahl DESC) as Rang
	FROM ParteienImBundestag p, ZweitStimmenNachBundesland z,
		Divisoren d, ZufallsZahlenSitzeNachLandeslisten rnd
	WHERE p.ParteiID = z.ParteiID 
		AND rnd.Zeile = MOD(p.ParteiID + ( SELECT COUNT(*) FROM
			Partei)*(z.BundeslandID + ( SELECT COUNT(*) FROM Bundesland)*(d.wert / 1)), ( SELECT COUNT(*) FROM ZufallsZahlenSitzeNachLandeslisten))), 

SitzeNachLandeslisten AS (
	SELECT z.ParteiID, BundeslandID, COUNT(Rang) as AnzahlSitze
	FROM ZugriffsreihenfolgeSitzeNachLandeslisten z,
		SitzeNachPartei s
	WHERE z.ParteiID = s.ParteiID 
		AND z.Rang <= s.AnzahlSitze
	GROUP BY z.ParteiID, z.BundeslandID, s.ParteiID), 

ListenKandidaten AS (
	SELECT ID
	FROM Kandidat
	WHERE BundeslandID IS NOT NULL EXCEPT
	SELECT KandidatID
	FROM Direktmandate ), 

ListenKandidatenMitRang AS (
	SELECT lk.ID, k.ParteiID, b.ID AS BundeslandID, ROW_NUMBER()
		OVER (PARTITION BY b.ID, k.ParteiID ORDER BY k.Listenplatz)
		AS Rang
	FROM ListenKandidaten lk, Bundesland b, Kandidat k
	WHERE lk.ID = k.ID 
		AND k.BundeslandID = b.ID ), 

BundeslandParteiZuDirektmandate AS (
	SELECT w.BundeslandID, d.ParteiID, Count(d.KandidatID) as
		Anzahl
	FROM Direktmandate d, Wahlkreis w
	WHERE d.DMWahlkreisID = w.ID
	GROUP BY w.BundeslandID, d.ParteiID), 

Abgeordnete AS (
	SELECT KandidatID
	FROM Direktmandate

	UNION

	SELECT lkr.ID
	FROM ListenKandidatenMitRang lkr LEFT OUTER JOIN
		BundeslandParteiZuDirektmandate b ON lkr.BundeslandID =
		b.BundeslandID AND lkr.ParteiID = b.ParteiID,
		SitzeNachLandeslisten s
	WHERE s.ParteiID = lkr.ParteiID 
		AND s.BundeslandID = lkr.BundeslandID 
		AND lkr.Rang <= s.AnzahlSitze - COALESCE(b.Anzahl, 0))
SELECT k.Vorname, k.Nachname, p.Kuerzel
FROM Abgeordnete a, Kandidat k LEFT OUTER JOIN Partei p ON
	k.ParteiID = p.ID
WHERE a.KandidatID = k.ID
ORDER BY k.Vorname, k.Nachname

