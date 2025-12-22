module Gritum.Rules.Impl.TotalClosingCostsMissing

open System
open Gritum.Model
open Gritum.Generated.RuleSpec

let id : RuleId =
    RuleId.TotalClosingCostsMissing

let title =
    "Total closing costs are missing"

let severity =
    Severity.High

let private mkFinding (docType: DocumentType) : Finding =
    let docLabel =
        match docType with
        | DocumentType.LoanEstimate -> "Loan Estimate"
        | DocumentType.ClosingDisclosure -> "Closing Disclosure"

    let msg =
        $"Total Closing Costs is missing on {docLabel}. " +
        "Without this value, the document cannot be evaluated for closing cost accuracy."

    { ruleId = id
      severity = severity
      evidence = []
      message = msg }

let private docTypeOfSnapshot (snap: DocumentSnapshot) : DocumentType =
    match snap with
    | LE _ -> DocumentType.LoanEstimate
    | CD _ -> DocumentType.ClosingDisclosure

let private contentOfSnapshot (snap: DocumentSnapshot) : DocumentContent =
    match snap with
    | LE c -> c
    | CD c -> c

let private filterByDocType (docType: DocumentType) (snaps: DocumentSnapshot list) : DocumentSnapshot list =
    snaps
    |> List.filter (fun s -> docTypeOfSnapshot s = docType)

/// Representative selection:
/// 1) if none -> MissingDocument
/// 2) consider only effectiveDate=Some
/// 3) pick max effectiveDate
/// 4) if tie on max -> InvalidField(docType, EffectiveDate, OutOfRange)
/// 5) if all None -> InvalidField(docType, EffectiveDate, ParseFailure)
let private pickRepresentative (docType: DocumentType) (snaps: DocumentSnapshot list) : Result<DocumentSnapshot, RuleError> =
    let candidates = filterByDocType docType snaps

    match candidates with
    | [] ->
        Error (MissingDocument docType)

    | [ single ] ->
        // Even if effectiveDate is None, if there is only one doc we accept it as representative.
        // If you'd rather require effectiveDate always, remove this branch.
        Ok single

    | many ->
        let withDates =
            many
            |> List.choose (fun s ->
                match (contentOfSnapshot s).effectiveDate with
                | Some d -> Some (d, s)
                | None -> None)

        match withDates with
        | [] ->
            Error (InvalidField (docType, FieldName.EffectiveDate, InvalidReason.ParseFailure))

        | _ ->
            let maxDate =
                withDates
                |> List.maxBy fst
                |> fst

            let top =
                withDates
                |> List.filter (fun (d, _) -> d = maxDate)
                |> List.map snd

            match top with
            | [ winner ] ->
                Ok winner
            | _ ->
                Error (InvalidField (docType, FieldName.EffectiveDate, InvalidReason.OutOfRange))

let private hasTotalClosingCosts (snap: DocumentSnapshot) : bool =
    let c = contentOfSnapshot snap
    c.totalClosingCosts |> Option.isSome

let check : Check =
    fun (input: PrecheckInput) ->
        // Representative LE
        match pickRepresentative DocumentType.LoanEstimate input.documentSnapshots with
        | Error e ->
            Error [ e ]

        | Ok leRep ->
            if not (hasTotalClosingCosts leRep) then
                Error [ MissingField (DocumentType.LoanEstimate, FieldName.TotalClosingCosts) ]
            else
                // Representative CD
                match pickRepresentative DocumentType.ClosingDisclosure input.documentSnapshots with
                | Error e ->
                    Error [ e ]
                | Ok cdRep ->
                    if not (hasTotalClosingCosts cdRep) then
                        Error [ MissingField (DocumentType.ClosingDisclosure, FieldName.TotalClosingCosts) ]
                    else
                        Ok None

let rule : Rule =
    { id = id
      check = check }