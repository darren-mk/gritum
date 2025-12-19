module Gritum.Rules

open Gritum.Model

// -----------------
// Rule implementations
// -----------------


let doesDsMissTotalClosingCosts (ds:DocumentSnapshot) : bool =
    let content =
        match ds with
        | LE content -> content
        | CD content -> content
    match content.totalClosingCosts with
    | Some _ -> false
    | None -> true

let checkTotalClosingCosts (p:PrecheckInput) : RuleCheckResult =
    let docSnapshotsMissingCosts: DocumentSnapshot list =
        List.filter doesDsMissTotalClosingCosts p.documentSnapshots
    if not (List.isEmpty docSnapshotsMissingCosts)
    then
        let sayMissingField (ds:DocumentSnapshot) =
            match ds with
            | LE _ -> MissingField LoanEstimate
            | CD _ -> MissingField ClosingDisclosure
        let ruleErrors: RuleErrors =
            List.map sayMissingField docSnapshotsMissingCosts
        Error ruleErrors
    else
        Ok None

let totalClosingCostsRule : Rule =
    { id = RuleId "TCC-001"
      check = checkTotalClosingCosts }

// -----------------
// Registry
// -----------------

let all : Rules =
    [ totalClosingCostsRule ]