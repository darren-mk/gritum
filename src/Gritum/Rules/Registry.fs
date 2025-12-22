module Gritum.Rules.Registry

open Gritum.Model
open Gritum.Rules.Impl

let all : Rules =
    [ TotalClosingCostsMissing.rule ]