package com.axiommd

import org.scalatest._,  wordspec._, matchers._



class FirstTestJs extends AnyWordSpec with should.Matchers{
  "this" should {
    "work" in {
      true should be(true)
    }
  }
}
