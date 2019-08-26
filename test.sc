val host = "alekslitvinenk.com"
val hostParts = host.split('.')
val redirectHost = if(hostParts.size == 3 && hostParts(0) == "www") hostParts.drop(1).mkString(".") else host