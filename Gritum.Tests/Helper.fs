module Helper

open Gritum.Model

let money (x: decimal) =
    match Money.create x with
    | Ok (m: Money) -> m
    | Error (e: string) -> failwith e