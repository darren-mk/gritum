module Gritum.Generated.RuleSpec

/// Generated from spec/rules/*.yaml. Do not edit by hand.

[<RequireQualifiedAccess>]
type RuleId =
    | TotalClosingCostsMissing
    | TotalClosingCostsSumMismatch

[<RequireQualifiedAccess>]
module RuleId =
    let toString (id: RuleId) : string =
        match id with
        | RuleId.TotalClosingCostsMissing -> "total-closing-costs-missing"
        | RuleId.TotalClosingCostsSumMismatch -> "total-closing-costs-sum-mismatch"
