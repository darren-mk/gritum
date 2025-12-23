open System
open System.IO

let srcDir =
    __SOURCE_DIRECTORY__

let rulesDir =
    Path.Combine(srcDir, "..", "spec", "rules")

let outputPath =
    [|srcDir; ".."; "src"; "Gritum"; "Generated"; "RuleSpec.fs"|]

let outputFile =
    Path.Combine outputPath

let kebabToPascal (s: string) =
    s.Split('-')
    |> Array.map (fun part ->
        part.Substring(0, 1).ToUpperInvariant() + part.Substring(1))
    |> String.concat ""

let extractRuleId (filePath: string) =
    File.ReadAllLines(filePath)
    |> Array.tryPick (fun line ->
        if line.StartsWith("id:") then
            Some (line.Replace("id:", "").Trim())
        else None)

let ruleIds =
    Directory.GetFiles(rulesDir, "*.yaml")
    |> Array.choose extractRuleId
    |> Array.distinct
    |> Array.sort

let cases =
    ruleIds
    |> Array.map (fun id ->
        sprintf "    | %s" (kebabToPascal id))
    |> String.concat Environment.NewLine

let toStringCases =
    ruleIds
    |> Array.map (fun id ->
        sprintf "        | RuleId.%s -> \"%s\"" (kebabToPascal id) id)
    |> String.concat Environment.NewLine

let content =
    $"""module Gritum.RuleSpec

/// Generated from spec/rules/*.yaml. Do not edit by hand.

[<RequireQualifiedAccess>]
type RuleId =
{cases}

[<RequireQualifiedAccess>]
module RuleId =
    let toString (id: RuleId) : string =
        match id with
{toStringCases}
"""

Directory.CreateDirectory(Path.GetDirectoryName(outputFile)) |> ignore
File.WriteAllText(outputFile, content)

printfn "Generated %d rule ids." ruleIds.Length