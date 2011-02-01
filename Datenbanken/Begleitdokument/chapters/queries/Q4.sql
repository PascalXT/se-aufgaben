INSERT INTO WahlkreisSieger(WahlkreisID, BundeslandID, P1, P2)

WITH 

MaxZweitStimmenNachWahlkreis AS (
	SELECT we.WahlkreisID, MAX(we.Anzahl) AS MaxStimmen
	FROM ZweitStimmenNachWahlkreis we
	WHERE we.Jahr = 2009
	GROUP BY WahlkreisID), 

MaxErststimmenNachWahlkreis AS (
	SELECT v.WahlkreisID, MAX(v.Anzahl) AS MaxStimmen
	FROM ErstStimmenNachWahlkreis v
	WHERE v.Jahr = 2009
	GROUP BY v.WahlkreisID), 

GewinnerZweitstimmen AS (
	SELECT we.WahlkreisID, we.ParteiID
	FROM ZweitStimmenNachWahlkreis we,
		MaxZweitStimmenNachWahlkreis ms
	WHERE we.WahlkreisID = ms.WahlkreisID 
		AND we.Anzahl = ms.MaxStimmen 
		AND we.Jahr = 2009), 

GewinnerErststimmen AS (
	SELECT we.WahlkreisID, we.KandidatID
	FROM ErstStimmenNachWahlkreis we,
		MaxErststimmenNachWahlkreis ms
	WHERE we.WahlkreisID = ms.WahlkreisID 
		AND we.Anzahl = ms.MaxStimmen 
		AND we.Jahr = 2009)
		
SELECT g1.WahlkreisID, wk.BundeslandID, p1.Kuerzel AS P1,
	p2.Kuerzel AS P2
FROM GewinnerErststimmen g1, GewinnerZweitstimmen g2,
	Partei p1, Partei p2, Kandidat k, Wahlkreis wk
WHERE g1.WahlkreisID = g2.WahlkreisID 
	AND g1.KandidatID = k.ID 
	AND k.ParteiID = p1.ID 
	AND g2.ParteiID = p2.ID 
	AND wk.ID = g1.WahlkreisID

	
	
	
SELECT *
FROM WahlkreisSieger




WITH 

GewinnerErststimmen(BundeslandID, Partei,
GewonneneWahlkreise) AS (
	SELECT BundeslandID, P1, COUNT(*)
	FROM WahlkreisSieger
	GROUP BY BundeslandID, P1 ), 

GewinnerZweitStimmen(BundeslandID, Partei,
GewonneneWahlkreise) AS (
	SELECT BundeslandID, P2, COUNT(*)
	FROM WahlkreisSieger
	GROUP BY BundeslandID, P2 ), 

GewinnerGesamt(BundeslandID, Partei,
GewonneneWahlkreise) AS (
	SELECT g1.BundeslandID, g1.Partei, g1.GewonneneWahlkreise +
		g2.GewonneneWahlkreise
	FROM GewinnerErststimmen g1, GewinnerZweitStimmen g2
	WHERE g1.BundeslandID = g2.BundeslandID 
		AND g1.Partei = g2.Partei), 

MaxBundeslandGewonneneWahlkreise AS (
	SELECT BundeslandID, Max(GewonneneWahlkreise) As MaxWk
	FROM GewinnerGesamt
	GROUP BY BundeslandID)
SELECT g.BundeslandID, g.Partei
FROM GewinnerGesamt g, MaxBundeslandGewonneneWahlkreise m
WHERE g.GewonneneWahlkreise = m.MaxWk 
	AND g.BundeslandID = m.BundeslandID

