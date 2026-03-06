/**
 * 채팅방별로 수락/거절/종료한 화상 세션 ID를 유지.
 * 채팅방을 나갔다 들어와도 수락·거절 바가 다시 뜨지 않도록 함.
 */
const byRoom = new Map<number, Set<number>>()

export function getHandledSessionIds(roomId: number): Set<number> {
  let set = byRoom.get(roomId)
  if (!set) {
    set = new Set<number>()
    byRoom.set(roomId, set)
  }
  return set
}

export function addHandledSession(roomId: number, sessionId: number): void {
  getHandledSessionIds(roomId).add(sessionId)
}

export function removeHandledSession(roomId: number, sessionId: number): void {
  getHandledSessionIds(roomId).delete(sessionId)
}
