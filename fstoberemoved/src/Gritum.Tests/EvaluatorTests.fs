module Gritum.Evaluator.Tests

open Xunit
open Gritum.Model
open Gritum.Evaluator
open Gritum.Generated.RuleSpec
open Gritum.TestHelper

let findingHigh : Finding =
    { ruleId = RuleId.TotalClosingCostsSumMismatch
      severity = Severity.High
      evidence = ([] : Evidence list)
      message = "high" }

let findingLow : Finding =
    { ruleId = RuleId.TotalClosingCostsMissing
      severity = Severity.Low
      evidence = ([] : Evidence list)
      message = "low" }

// ... summarizeStatus tests unchanged ...

// runRules tests (using local stub rules to force findings)

let mkRule (id: RuleId) (check: Check) : Rule =
    { id = id; check = check }

[<Fact>]
let ``runRules - High finding yields Critical`` () =
    let input = mkInput []
    let rules : Rules =
        [ mkRule RuleId.TotalClosingCostsMissing (fun _ -> Ok (Some findingHigh)) ]
    let findings, errors, status = runRules input rules
    Assert.Equal(PrecheckStatus.Critical, status)
    Assert.Empty(errors)
    Assert.Single(findings)

[<Fact>]
let ``runRules - Low finding yields Advisory`` () =
    let input = mkInput []
    let rules : Rules =
        [ mkRule RuleId.TotalClosingCostsMissing (fun _ -> Ok (Some findingLow)) ]
    let findings, errors, status = runRules input rules
    Assert.Equal(PrecheckStatus.Advisory, status)
    Assert.Empty(errors)
    Assert.Single(findings)

[<Fact>]
let ``runRules - mixed Low and High yields Critical`` () =
    let input = mkInput []
    let rules : Rules =
        [ mkRule RuleId.TotalClosingCostsMissing (fun _ -> Ok (Some findingLow))
          mkRule RuleId.TotalClosingCostsSumMismatch (fun _ -> Ok (Some findingHigh)) ]
    let findings, errors, status = runRules input rules
    Assert.Equal(PrecheckStatus.Critical, status)
    Assert.Empty(errors)
    Assert.Equal(2, findings.Length)

[<Fact>]
let ``runRules - any rule error yields Inconclusive even if findings exist`` () =
    let input = mkInput []
    let rules : Rules =
        [ mkRule RuleId.TotalClosingCostsMissing (fun _ -> Ok (Some findingHigh))
          mkRule RuleId.TotalClosingCostsSumMismatch (fun _ -> Error [ MissingField (LoanEstimate, TotalClosingCosts) ]) ]
    let findings, errors, status = runRules input rules
    Assert.Equal(PrecheckStatus.Inconclusive, status)
    Assert.NotEmpty(findings)
    Assert.Single(errors) |> ignore
    Assert.Contains(MissingField (LoanEstimate, TotalClosingCosts), errors)