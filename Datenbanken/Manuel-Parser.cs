using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using System.Text.RegularExpressions;
using System.Globalization;
using System.IO;

namespace Bundestagswahl_Parser
{
    class Program
    {
        static readonly Regex ListRegex = new Regex("<tbody>(?<content>.*?)</tbody>", RegexOptions.Compiled | RegexOptions.Singleline);
        static readonly Regex SimpleListContentRegex = new Regex("<tr><td class=\"lb\"><a href=\"(?<link>.*?)\" title=\"(?<name>.*?)\">.*?</a></td></tr>", RegexOptions.Compiled);
        static readonly Regex VoteListContentRegex = new Regex("<tr>\\s*?<td class=\"lb\">(?<name>.*?)</td>\\s*?<td>(?<erst>.*?)</td>\\s*?<td>.*?</td>\\s*?<td align=\"center\">.*?</td>\\s*?<td>(?<zweit>.*?)</td>\\s*?<td align=\"center\">.*?</td>\\s*?<td align=\"center\">.*?</td>\\s*?</tr>", RegexOptions.Compiled);

        static readonly Regex WahlkreisNameRegex = new Regex("(?<name>.*?) \\((?<nr>[0-9]*?)\\)", RegexOptions.Compiled);

        static readonly NumberFormatInfo DeNumberFormat = new CultureInfo("de-DE").NumberFormat;

        static int CurParteienDictKey = 1;
        static readonly Dictionary<string, int> ParteienDict = new Dictionary<string, int>();


        static void Main(string[] args)
        {
            string resultsUrl = "http://www.bundeswahlleiter.de/de/bundestagswahlen/BTW_BUND_09/ergebnisse/wahlkreisergebnisse/";

            string bundeslandPageContent = DownloadPage(resultsUrl);
            if (bundeslandPageContent == null)
            {
                Console.WriteLine("Bundesländer konnten nicht geladen werden. Abbruch.");
                return;
            }
            SimpleListItem[] bundeslandList = ParseSimpleList(bundeslandPageContent);


            List<Bundesland> bundeslaender = new List<Bundesland>();
            foreach(var curBundesland in bundeslandList)
            {
                Bundesland newBL = new Bundesland();
                newBL.Name = curBundesland.Name;
                newBL.Wahlkreise = new List<Wahlkreis>();
                string blUri = resultsUrl + curBundesland.Link;

                Console.WriteLine("Parse Bundesland " + curBundesland.Name + " ...");

                //Parse Wahlkreise
                string blWahlkreisePageContent = DownloadPage(blUri);
                if (blWahlkreisePageContent == null)
                {
                    Console.WriteLine("Wahlkreise konnten nicht geladen werden. Abbruch.");
                    return;
                }
                SimpleListItem[] blWahlkreiseList = ParseSimpleList(blWahlkreisePageContent);
                foreach (var curWahlkreis in blWahlkreiseList)
                {
                    Wahlkreis newWK = new Wahlkreis();

                    Match wkNameMatch = WahlkreisNameRegex.Match(curWahlkreis.Name);
                    if (wkNameMatch.Success)
                    {
                        newWK.Name = wkNameMatch.Groups["name"].Value;
                        newWK.Nr = int.Parse(wkNameMatch.Groups["nr"].Value);
                    }
                    else
                        newWK.Name = curWahlkreis.Name;

                    Console.WriteLine("  Parse Wahlkreis " + newWK.Name + " ...");

                    //Parse Ergebnisse
                    string wkErgebnisPageContent = DownloadPage(blUri + curWahlkreis.Link);
                    if (wkErgebnisPageContent == null)
                    {
                        Console.WriteLine("Wahlkreise-Ergebnis konnte nicht geladen werden. Abbruch.");
                        return;
                    }

                    var wkVotes = ParseVoteList(wkErgebnisPageContent);
                    newWK.Wahlberechtigte = wkVotes["Wahlberechtigte"]; wkVotes.Remove("Wahlberechtigte");
                    newWK.Waehler = wkVotes["Wähler"]; wkVotes.Remove("Wähler");
                    newWK.Ungueltige = wkVotes["Ungültige"]; wkVotes.Remove("Ungültige");
                    newWK.Gueltige = wkVotes["Gültige"]; wkVotes.Remove("Gültige");

                    newWK.Parteien = new List<Partei>();
                    foreach (var curPartei in wkVotes)
                    {
                        Partei newPartei = new Partei() { Id=GetParteiId(curPartei.Key), Ergebnis=curPartei.Value};
                        newWK.Parteien.Add(newPartei);
                    }

                    newBL.Wahlkreise.Add(newWK);
                }

                bundeslaender.Add(newBL);
            }

            //Write to CSV

            StreamWriter csvWriter = new StreamWriter("Ergebnisse2009.csv", false, Encoding.UTF8);

            csvWriter.WriteLine("Bundestagswahl 2009");
            csvWriter.WriteLine("Endgültig");

            csvWriter.Write("Nr,Gebiet,gehört,Wahlberechtigte,,,,Wähler,,,,Ungültige,,,,Gültige,,,,");
            foreach (string curPartei in ParteienDict.Keys)
                csvWriter.Write(curPartei + ",,,,");
            csvWriter.Write(Environment.NewLine);
            csvWriter.Write(",,zu,");
            for (int a = 1; a < CurParteienDictKey + 4; a++)
                csvWriter.Write("Erststimmen,,Zweitstimmen,,");
            csvWriter.Write(Environment.NewLine);
            csvWriter.Write(",,,");
            for (int a = 1; a < CurParteienDictKey + 4; a++)
                csvWriter.Write("Endgültig,,");

            int CurBundeslandId = 1;
            foreach (var curBL in bundeslaender)
            {
                foreach(var curWK in curBL.Wahlkreise)
                {
                    csvWriter.Write(curWK.Nr + ",");
                    csvWriter.Write("\"" + curWK.Name + "\",");
                    csvWriter.Write(CurBundeslandId + ",");

                    WriteVotes(csvWriter, curWK.Wahlberechtigte);
                    WriteVotes(csvWriter, curWK.Waehler);
                    WriteVotes(csvWriter, curWK.Ungueltige);
                    WriteVotes(csvWriter, curWK.Gueltige);

                    foreach (var curPartei in ParteienDict)
                    {
                        bool foundPartei = false;
                        foreach (var curWkPartei in curWK.Parteien)
                        {
                            if (curPartei.Value == curWkPartei.Id)
                            {
                                WriteVotes(csvWriter, curWkPartei.Ergebnis);
                                foundPartei = true;
                            }
                        }
                        if (!foundPartei)
                            csvWriter.Write(",,,,");
                    }
                    csvWriter.Write(Environment.NewLine);
                }

                csvWriter.Write(CurBundeslandId + ",");
                csvWriter.Write("\"" + curBL.Name + "\",");
                csvWriter.Write("99");
                csvWriter.Write(Environment.NewLine);
                csvWriter.Write(Environment.NewLine);

                CurBundeslandId++;
            }

            csvWriter.Write("99,\"Bundesgebiet\"");
            csvWriter.Write(Environment.NewLine);

            csvWriter.Close();


            Console.ReadLine();
        }

        static void WriteVotes(StreamWriter writer, Stimmen votes)
        {
            writer.Write((votes.Erststimmen > 0 ? votes.Erststimmen.ToString() : "") + ",," + (votes.Zweitstimmen > 0 ? votes.Zweitstimmen.ToString() : "") + ",,");
        }

        public static string DownloadPage(string uri)
        {
            var webRequest = (HttpWebRequest)HttpWebRequest.Create(uri);
            HttpWebResponse webResponse = (HttpWebResponse)webRequest.GetResponse();
            if (webResponse.StatusCode != HttpStatusCode.OK)
                return null;

            var responseStream = webResponse.GetResponseStream();

            StringBuilder responseStringBuilder = new StringBuilder();
            byte[] responseData = new byte[8096];
            while (true)
            {
                int bytesRead = 0;
                try
                {
                    bytesRead = responseStream.Read(responseData, 0, responseData.Length);
                }
                catch
                {
                    return null;
                }

                if (bytesRead == 0)
                    break;
                responseStringBuilder.Append(Encoding.UTF8.GetString(responseData, 0, bytesRead));
            }
            responseStream.Close();
            return responseStringBuilder.ToString();
        }

        static SimpleListItem[] ParseSimpleList(string pageContent)
        {
            List<SimpleListItem> listItems = new List<SimpleListItem>();

            var listMatch = ListRegex.Match(pageContent);
            if(!listMatch.Success)
                return null;

            var listMatches = SimpleListContentRegex.Matches(listMatch.Groups["content"].Value);
            foreach(Match curMatch in listMatches)
            {
                SimpleListItem newListItem = new SimpleListItem() {
                    Link = curMatch.Groups["link"].Value,
                    Name = curMatch.Groups["name"].Value};

                listItems.Add(newListItem);
            }
            return listItems.ToArray();
        }

        static Dictionary<string, Stimmen> ParseVoteList(string pageContent)
        {
            Dictionary<string, Stimmen> resultItems = new Dictionary<string, Stimmen>();

            var listMatch = ListRegex.Match(pageContent);
            if (!listMatch.Success)
                return null;

            var listMatches = VoteListContentRegex.Matches(listMatch.Groups["content"].Value);
            foreach (Match curMatch in listMatches)
            {
                resultItems.Add(curMatch.Groups["name"].Value,
                    new Stimmen() { Erststimmen = ParseVoteNumber(curMatch.Groups["erst"].Value),
                                    Zweitstimmen = ParseVoteNumber(curMatch.Groups["zweit"].Value) });
            }
            return resultItems;
        }

        static int ParseVoteNumber(string voteStr)
        {
            if(voteStr.Equals("-"))
                return 0;
            else
                return int.Parse(voteStr, NumberStyles.AllowThousands, DeNumberFormat);
        }

        static int GetParteiId(string name)
        {
            if (name.StartsWith("K:"))
                name = "PARTEILOS";

            if (!ParteienDict.ContainsKey(name))
                ParteienDict.Add(name, CurParteienDictKey++);
            return ParteienDict[name];
        }

        struct SimpleListItem
        {
            public string Name;
            public string Link;
        }

        struct Bundesland
        {
            public string Name;
            public List<Wahlkreis> Wahlkreise;
        }

        struct Wahlkreis
        {
            public int Nr;
            public string Name;

            public Stimmen Wahlberechtigte;
            public Stimmen Waehler;
            public Stimmen Gueltige;
            public Stimmen Ungueltige;

            public List<Partei> Parteien;
        }

        struct Partei
        {
            public int Id;
            public Stimmen Ergebnis;
        }

        struct Stimmen
        {
            public int Erststimmen;
            public int Zweitstimmen;
        }
    }
}
