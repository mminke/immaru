import React from 'react';

let _hotkeysEnabled = true

export function disableHotkeys() {
    _hotkeysEnabled = false
}

export function enableHotkeys() {
    _hotkeysEnabled = true
}

export const hotkeysEnabledFilter = ( handler: (event:any) => void  ) => {
    return (event:any) => {
        if(_hotkeysEnabled) {
            event.preventDefault()
            handler(event)
        }
    }
}
