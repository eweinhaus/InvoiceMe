import { useMemo, useState } from 'react'

interface AvatarProps {
  name?: string
  email?: string
  picture?: string
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

// Generate a color based on a string (for consistent avatar colors)
function getColorFromString(str: string): string {
  const colors = [
    'bg-blue-500',
    'bg-green-500',
    'bg-purple-500',
    'bg-pink-500',
    'bg-indigo-500',
    'bg-yellow-500',
    'bg-red-500',
    'bg-teal-500',
    'bg-orange-500',
    'bg-cyan-500',
  ]
  
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    hash = str.charCodeAt(i) + ((hash << 5) - hash)
  }
  
  return colors[Math.abs(hash) % colors.length]
}

// Extract initials from a name or email
function getInitials(name?: string, email?: string): string {
  // Handle empty strings as well as undefined
  const nameValue = name?.trim()
  const emailValue = email?.trim()
  
  if (nameValue && nameValue.length > 0) {
    const parts = nameValue.split(/\s+/).filter(p => p.length > 0)
    if (parts.length >= 2) {
      // First letter of first name + first letter of last name
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
    } else if (parts.length === 1 && parts[0].length > 0) {
      // First two letters if only one name
      return parts[0].substring(0, 2).toUpperCase()
    }
  }
  
  if (emailValue && emailValue.length > 0) {
    // Use first letter of email (before @)
    const emailPart = emailValue.split('@')[0]
    if (emailPart.length >= 2) {
      return emailPart.substring(0, 2).toUpperCase()
    }
    if (emailPart.length === 1) {
      return emailPart[0].toUpperCase()
    }
  }
  
  // Fallback to "U" for User instead of "?"
  return 'U'
}

const sizeClasses = {
  sm: 'w-8 h-8 text-xs',
  md: 'w-10 h-10 text-sm',
  lg: 'w-12 h-12 text-base',
}

export default function Avatar({ name, email, picture, size = 'sm', className = '' }: AvatarProps) {
  const [imageError, setImageError] = useState(false)
  const initials = useMemo(() => getInitials(name, email), [name, email])
  const bgColor = useMemo(() => {
    const source = (name?.trim() || email?.trim() || 'default')
    return getColorFromString(source)
  }, [name, email])
  
  const sizeClass = sizeClasses[size]

  const hasValidPicture = picture && picture.trim().length > 0

  // Only show picture if it's a valid non-empty string and hasn't failed to load
  if (hasValidPicture && !imageError) {
    return (
      <img
        src={picture}
        alt={name || email || 'User'}
        className={`${sizeClass} rounded-full object-cover ${className}`}
        onError={() => setImageError(true)}
      />
    )
  }

  // Always render initials - never show question mark
  return (
    <div
      className={`${sizeClass} ${bgColor} rounded-full flex items-center justify-center text-white font-semibold ${className}`}
      title={name || email || 'User'}
      role="img"
      aria-label={`Avatar for ${name || email || 'User'}`}
    >
      {initials}
    </div>
  )
}

